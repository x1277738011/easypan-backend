package com.easypan.service.impl;

import com.easypan.componet.RedisComponet;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
//import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.enums.*;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.query.SimplePage;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.FileInfoMapper;
import com.easypan.mappers.UserInfoMapper;
import com.easypan.service.FileInfoService;
import com.easypan.utils.DateUtil;
//import com.easypan.utils.ProcessUtils;
//import com.easypan.utils.ScaleFilter;
import com.easypan.utils.ProcessUtils;
import com.easypan.utils.ScaleFilter;
import com.easypan.utils.StringTools;
import org.apache.catalina.User;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 文件信息 业务接口实现
 */
@Service("fileInfoService")
public class FileInfoServiceImpl implements FileInfoService {

    private static final Logger logger = LoggerFactory.getLogger(FileInfoServiceImpl.class);

    @Resource
    @Lazy
    private FileInfoServiceImpl fileInfoService;

    @Resource
    private AppConfig appConfig;


    @Resource
    private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private RedisComponet redisComponent;


    /**
     * 根据条件查询列表
     */
    @Override
    public List<FileInfo> findListByParam(FileInfoQuery param) {
        return this.fileInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(FileInfoQuery param) {
        return this.fileInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<FileInfo> list = this.findListByParam(param);
        PaginationResultVO<FileInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(FileInfo bean) {
        return this.fileInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<FileInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.fileInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<FileInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.fileInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据FileIdAndUserId获取对象
     */
    @Override
    public FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId) {
        return this.fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
    }

    /**
     * 根据FileIdAndUserId修改
     */
    @Override
    public Integer updateFileInfoByFileIdAndUserId(FileInfo bean, String fileId, String userId) {
        return this.fileInfoMapper.updateByFileIdAndUserId(bean, fileId, userId);
    }

    /**
     * 根据FileIdAndUserId删除
     */
    @Override
    public Integer deleteFileInfoByFileIdAndUserId(String fileId, String userId) {
        return this.fileInfoMapper.deleteByFileIdAndUserId(fileId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResultDto uploadFile(SessionWebUserDto webUserDto,
                                      String fileId,
                                      MultipartFile file,
                                      String fileName,
                                      String filePid,
                                      String fileMd5,
                                      Integer chunkIndex,
                                      Integer chunks) {
        UploadResultDto resultDto = new UploadResultDto();
        Boolean uploadSuccess = true;
        File tempFileFolder = null;
        try {
            if (StringTools.isEmpty(fileId)) {
                fileId = StringTools.getRandomNumber(Constants.LENGTH_10);
            }
            resultDto.setFileId(fileId);
            Date curDate = new Date();
            // 获取用户可用空间
            UserSpaceDto spaceDto = redisComponent.getUserSpaceUse(webUserDto.getUserId());
            // 第一个文件，判断是否可秒传
            if (chunkIndex == 0) {
                // 封装查询条件
                FileInfoQuery infoQuery = new FileInfoQuery();
                // 文件MD5值
                infoQuery.setFileMd5(fileMd5);
                // 只取第一条
                infoQuery.setSimplePage(new SimplePage(0, 1));
                // 转码成功，使用中
                infoQuery.setStatus(FileStatusEnums.USING.getStatus());
                List<FileInfo> dbFileList = this.fileInfoMapper.selectList(infoQuery);
                //秒传
                if (!dbFileList.isEmpty()) {
                    FileInfo dbFile = dbFileList.get(0);
                    //判断文件状态,如果空间不足
                    if (dbFile.getFileSize() + spaceDto.getUseSpace() > spaceDto.getTotalSpace()) {
                        throw new BusinessException(ResponseCodeEnum.CODE_904);
                    }
                    dbFile.setFileId(fileId);
                    dbFile.setFilePid(filePid);
                    dbFile.setUserId(webUserDto.getUserId());
                    dbFile.setCreateTime(curDate);
                    dbFile.setLastUpdateTime(curDate);
                    dbFile.setStatus(FileStatusEnums.USING.getStatus());
                    dbFile.setDelFlag(FileDelFlagEnums.USING.getFlag());
                    dbFile.setFileMd5(fileMd5);
                    //文件重命名
                    fileName = autoRename(filePid, webUserDto.getUserId(), fileName);
                    dbFile.setFileName(fileName);
                    this.fileInfoMapper.insert(dbFile);
                    resultDto.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());
                    //更新用户空间使用
                    updateUserSpace(webUserDto, dbFile.getFileSize());
                    return resultDto;
                }
            }
                //先放入redis中作为临时存储
                Long currentTempSize = redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
                //判断磁盘空间(分片+临时+已使用>总？抛异常:继续）
                if (file.getSize() + currentTempSize + spaceDto.getUseSpace() > spaceDto.getTotalSpace()) {
                    throw new BusinessException(ResponseCodeEnum.CODE_904);
                }

                //暂存在临时目录 d:/easypan/temp/
                String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
                String currentUserFolderName = webUserDto.getUserId() + fileId;
                //创建临时目录 d:/easypan/temp/{userId}/{fileId}
                tempFileFolder = new File(tempFolderName + currentUserFolderName);
                if (!tempFileFolder.exists()) {
                    tempFileFolder.mkdirs();
                }
                File newFile = new File(tempFileFolder.getPath() + "/" + chunkIndex);
                file.transferTo(newFile);
                if (chunkIndex < chunks - 1){
                    resultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());
                    //保存临时大小
                    redisComponent.saveFileTempSize(webUserDto.getUserId(),fileId,file.getSize());
                    return resultDto;
                }
                //最后一个分片上传完成，记录数据库异步合并封分片
                String month = DateUtil.format(new Date(),DateTimePatternEnum.YYYY_MM.getPattern());
                String fileSuffix = StringTools.getFileSuffix(fileName);
                // 真实文件名
                // userId + fileId.fileSuffix
                String realFileName = currentUserFolderName + fileSuffix;
                // 根据后缀从枚举类中获取文件类别
                FileTypeEnums fileTypeEnums = FileTypeEnums.getFileTypeBySuffix(fileSuffix);

                //自动重命名
                fileName = autoRename(filePid, webUserDto.getUserId(), fileName);
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileId(fileId);
                fileInfo.setUserId(webUserDto.getUserId());
                fileInfo.setFileMd5(fileMd5);
                fileInfo.setFileName(fileName);
                fileInfo.setFilePath(month + "/" + realFileName);
                fileInfo.setFilePid(filePid);
                fileInfo.setCreateTime(curDate);
                fileInfo.setLastUpdateTime(curDate);
                fileInfo.setFileCategory(fileTypeEnums.getCategory().getCategory());
                fileInfo.setFileType(fileTypeEnums.getType());
                fileInfo.setStatus(FileStatusEnums.TRANSFER.getStatus());
                fileInfo.setFolderType(FileFolderTypeEnums.FILE.getType());
                fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
                this.fileInfoMapper.insert(fileInfo);

                Long totalSize = redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
                updateUserSpace(webUserDto, totalSize);
                resultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());
                // 事务提交后执行
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        fileInfoService.transferFile(fileInfo.getFileId(),webUserDto);
                    }
                });
                return resultDto;

        }catch (BusinessException e){
            logger.error("文件上传失败:",e);
            uploadSuccess = false;
            throw e;
        }catch (Exception e){
            logger.error("文件上传失败:",e);
            uploadSuccess = false;
        }finally {
            if (!uploadSuccess && tempFileFolder != null){
                try {
                    FileUtils.deleteDirectory(tempFileFolder);
                } catch (IOException e) {
                    logger.error("删除临时目录失败:",e);
                }
            }
        }

        return resultDto;
        }

    /**
     * 自动命令文件
     * @param filePid
     * @param userId
     * @param fileName
     * @return
     */
    private String autoRename(String filePid, String userId, String fileName) {
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFilePid(filePid);
        fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
        fileInfoQuery.setFileName(fileName);
        Integer count = this.fileInfoMapper.selectCount(fileInfoQuery);
        if (count > 0) {
            return StringTools.rename(fileName);
        }

        return fileName;
    }

    /**
     * 更新文件大小
     * @param webUserDto
     * @param totalSize
     */
    private void updateUserSpace(SessionWebUserDto webUserDto, Long totalSize) {
        Integer count = userInfoMapper.updateUserSpace(webUserDto.getUserId(), totalSize, null);
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        UserSpaceDto spaceDto = redisComponent.getUserSpaceUse(webUserDto.getUserId());
        spaceDto.setUseSpace(spaceDto.getUseSpace() + totalSize);
        redisComponent.saveSysSettingDto(webUserDto.getUserId(), spaceDto);
    }

    /**
     * 转码
     * @param fileId
     * @param webUserDto
     */
    @Async
    public void transferFile(String fileId, SessionWebUserDto webUserDto) {
        boolean transferSuccess = true;
        String targetFilePath = null;
        String cover = null;
        FileTypeEnums fileTypeEnum = null;
        FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, webUserDto.getUserId());
        try {
            //如果不是转码中就不用处理了
            if (fileInfo == null || !FileStatusEnums.TRANSFER.getStatus().equals(fileInfo.getStatus())) {
                return;
            }
            //临时目录
            String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
            String currentUserFolderName = webUserDto.getUserId() + fileId;
            File fileFolder = new File(tempFolderName + "/" + currentUserFolderName);
            if (!fileFolder.exists()) {
                fileFolder.mkdirs();
            }
            //文件后缀
            String fileSuffix = StringTools.getFileSuffix(fileInfo.getFileName());
            String month = DateUtil.format(fileInfo.getCreateTime(), DateTimePatternEnum.YYYY_MM.getPattern());
            //目标目录 d:/easypan + /file + /{month}
            String targetFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFolder = new File(targetFolderName + "/" + month);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            //真实文件名 {userId+fileId}
            String realFileName = currentUserFolderName + fileSuffix;
            //真实文件路径
            targetFilePath = targetFolder.getPath() + "/" + realFileName;
            //合并文件
            /**
             * fileFolder.getPath() 临时目录
             * targetFilePath 目标目录
             * fileInfo.getFileName() 文件名
             * delSource
             */
            union(fileFolder.getPath(), targetFilePath, fileInfo.getFileName(), true);
            fileTypeEnum = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
            //如果file为视频else图片
            if (FileTypeEnums.VIDEO == fileTypeEnum) {
                //视频文件切割
                cutFile4Video(fileId, targetFilePath);
                //视频生成缩略图
                cover = month + "/" + currentUserFolderName + Constants.IMAGE_PNG_SUFFIX;
                String coverPath = targetFolderName + "/" + cover;
                ScaleFilter.createCover4Video(new File(targetFilePath), Constants.LENGTH_150, new File(coverPath));
            } else if (FileTypeEnums.IMAGE == fileTypeEnum) {
                //生成缩略图
                cover = month + "/" + realFileName.replace(".", "_.");
                String coverPath = targetFolderName + "/" + cover;
                Boolean created = ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath), Constants.LENGTH_150, new File(coverPath), false);
                // 如果没有生成缩略图，直接将原图复制当做缩略图
                if (!created) {
                    FileUtils.copyFile(new File(targetFilePath), new File(coverPath));
                }
            }
        } catch (Exception e) {
            logger.error("文件转码失败，文件Id:{},userId:{}", fileId, webUserDto.getUserId(), e);
            transferSuccess = false;
        } finally {
            FileInfo updateInfo = new FileInfo();
            updateInfo.setFileSize(new File(targetFilePath).length());
            updateInfo.setFileCover(cover);
            updateInfo.setStatus(transferSuccess ? FileStatusEnums.USING.getStatus() : FileStatusEnums.TRANSFER_FAIL.getStatus());
            fileInfoMapper.updateFileStatusWithOldStatus(fileId, webUserDto.getUserId(), updateInfo, FileStatusEnums.TRANSFER.getStatus());
        }
    }

    /**
     * 合并文件
     * @param dirPath
     * @param toFilePath
     * @param fileName
     * @param delSource
     * @throws BusinessException
     */
    public static void union(String dirPath, String toFilePath, String fileName, boolean delSource)
            throws BusinessException {
        // 目录不存在，直接抛异常
        File dir = new File(dirPath);
        if (!dir.exists()) {
            throw new BusinessException("目录不存在");
        }
        // 取出temp目录下的所有文件
        File[] fileList = dir.listFiles();
        File targetFile = new File(toFilePath);
        // RandomAccessFile支持"随机访问"的方式，程序可以直接跳转到文件的任意地方来读写数据。
        RandomAccessFile writeFile = null;
        try {
            // 目标目录
            writeFile = new RandomAccessFile(targetFile, "rw");
            byte[] b = new byte[1024 * 10];
            for (int i = 0; i < fileList.length; i++) {
                int len = -1;
                //创建读块文件的对象，分别取出 0, 1, 2 ...
                File chunkFile = new File(dirPath + File.separator + i);
                RandomAccessFile readFile = null;
                try {
                    readFile = new RandomAccessFile(chunkFile, "r");
                    while ((len = readFile.read(b)) != -1) {
                        writeFile.write(b, 0, len);
                    }
                } catch (Exception e) {
                    logger.error("合并分片失败", e);
                    throw new BusinessException("合并文件失败");
                } finally {
                    if (readFile != null) {
                        readFile.close();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("合并文件:{}失败", fileName, e);
            throw new BusinessException("合并文件" + fileName + "出错了");
        } finally {
            try {
                if (writeFile != null) {
                    writeFile.close();
                }
            } catch (IOException e) {
                logger.error("关闭流失败", e);
            }
            if (delSource) {
                if (dir.exists()) {
                    try {
                        // 以递归方式删除目录。
                        FileUtils.deleteDirectory(dir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 利用java代码操作命令行窗口执行FFmpeg对视频进行切割，生成.m3u8索引文件和.ts切片文件

    private void cutFile4Video(String fileId, String videoFilePath) {
        //创建同名切片目录
        File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
        if (!tsFolder.exists()) {
            tsFolder.mkdirs();
        }
        final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
        final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";

        String tsPath = tsFolder + "/" + Constants.TS_NAME;
        //生成index.ts
        String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
        ProcessUtils.executeCommand(cmd, false);//false 不生成日志
        //生成索引文件.m3u8 和切片.ts
        cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Constants.M3U8_NAME, tsFolder.getPath(), fileId);
        ProcessUtils.executeCommand(cmd, false);
        //删除index.ts
        new File(tsPath).delete();
    }

    /**
     * 检查文件名
     * @param filePid
     * @param userId
     * @param fileName
     * @param folderType
     */
    private void checkFileName(String filePid, String userId, String fileName, Integer folderType) {
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setFolderType(folderType);
        fileInfoQuery.setFileName(fileName);
        fileInfoQuery.setFilePid(filePid);
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
        Integer count = this.fileInfoMapper.selectCount(fileInfoQuery);
        if (count > 0) {
            throw new BusinessException("此目录下已存在同名文件，请修改名称");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileInfo newFolder(String filePid, String userId, String folderName) {
        checkFileName(filePid, userId, folderName, FileFolderTypeEnums.FOLDER.getType());
        Date curDate = new Date();
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(StringTools.getRandomString(Constants.LENGTH_10));
        fileInfo.setUserId(userId);
        fileInfo.setFilePid(filePid);
        fileInfo.setFileName(folderName);
        fileInfo.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        fileInfo.setCreateTime(curDate);
        fileInfo.setLastUpdateTime(curDate);
        fileInfo.setStatus(FileStatusEnums.USING.getStatus());
        fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
        this.fileInfoMapper.insert(fileInfo);

        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setFilePid(filePid);
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFileName(folderName);
        fileInfoQuery.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
        Integer count = this.fileInfoMapper.selectCount(fileInfoQuery);
        if (count > 1) {
            throw new BusinessException("文件夹" + folderName + "已经存在");
        }
        fileInfo.setFileName(folderName);
        fileInfo.setLastUpdateTime(curDate);
        return fileInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileInfo rename(String fileId, String userId, String fileName) {
        FileInfo fileInfo = this.fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        if (fileInfo == null) {
            throw new BusinessException("文件不存在");
        }
        if (fileInfo.getFileName().equals(fileName)) {
            return fileInfo;
        }
        String filePid = fileInfo.getFilePid();
        checkFileName(filePid, userId, fileName, fileInfo.getFolderType());
        //文件获取后缀
        if (FileFolderTypeEnums.FILE.getType().equals(fileInfo.getFolderType())) {
            fileName = fileName + StringTools.getFileSuffix(fileInfo.getFileName());
        }
        Date curDate = new Date();
        FileInfo dbInfo = new FileInfo();
        dbInfo.setFileName(fileName);
        dbInfo.setLastUpdateTime(curDate);
        this.fileInfoMapper.updateByFileIdAndUserId(dbInfo, fileId, userId);

        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setFilePid(filePid);
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFileName(fileName);
        fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
        Integer count = this.fileInfoMapper.selectCount(fileInfoQuery);
        if (count > 1) {
            throw new BusinessException("文件名" + fileName + "已经存在");
        }
        fileInfo.setFileName(fileName);
        fileInfo.setLastUpdateTime(curDate);
        return fileInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeFileFolder(String fileIds, String filePid, String userId) {
        if (fileIds.equals(filePid)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!Constants.ZERO_STR.equals(filePid)) {
            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(filePid, userId);
            if (fileInfo == null || !FileDelFlagEnums.USING.getFlag().equals(fileInfo.getDelFlag())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }
        String[] fileIdArray = fileIds.split(",");

        FileInfoQuery query = new FileInfoQuery();
        query.setFilePid(filePid);
        query.setUserId(userId);
        List<FileInfo> dbFileList = fileInfoService.findListByParam(query);

        Map<String, FileInfo> dbFileNameMap = dbFileList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (file1, file2) -> file2));
        //查询选中的文件
        query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFileIdArray(fileIdArray);
        List<FileInfo> selectFileList = fileInfoService.findListByParam(query);

        //将所选文件重命名
        for (FileInfo item : selectFileList) {
            FileInfo rootFileInfo = dbFileNameMap.get(item.getFileName());
            //文件名已经存在，重命名被还原的文件名
            FileInfo updateInfo = new FileInfo();
            if (rootFileInfo != null) {
                String fileName = StringTools.rename(item.getFileName());
                updateInfo.setFileName(fileName);
            }
            updateInfo.setFilePid(filePid);
            this.fileInfoMapper.updateByFileIdAndUserId(updateInfo, item.getFileId(), userId);
        }
    }

    /**
     * 删除
     * @param userId
     * @param fileIds
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFile2RecycleBatch(String userId, String fileIds) {
        String[] fileIdArray = fileIds.split(",");
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFileIdArray(fileIdArray);
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        List<FileInfo> fileInfoList = this.fileInfoMapper.selectList(query);
        if (fileInfoList.isEmpty()) {
            return;
        }
        List<String> delFilePidList = new ArrayList<>();
        for (FileInfo fileInfo : fileInfoList) {
            findAllSubFolderFileIdList(delFilePidList, userId, fileInfo.getFileId(), FileDelFlagEnums.USING.getFlag());
        }
        //将目录下的所有文件更新为已删除
        if (!delFilePidList.isEmpty()) {
            FileInfo updateInfo = new FileInfo();
            updateInfo.setDelFlag(FileDelFlagEnums.DEL.getFlag());
            this.fileInfoMapper.updateFileDelFlagBatch(updateInfo, userId, delFilePidList, null, FileDelFlagEnums.USING.getFlag());
        }

        //将选中的文件更新为回收站
        List<String> delFileIdList = Arrays.asList(fileIdArray);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setRecoveryTime(new Date());
        fileInfo.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        this.fileInfoMapper.updateFileDelFlagBatch(fileInfo, userId, null, delFileIdList, FileDelFlagEnums.USING.getFlag());
    }

    /**
     * 递归找出目录下所有文件
     * @param fileIdList
     * @param userId
     * @param fileId
     * @param delFlag
     */
    private void findAllSubFolderFileIdList(List<String> fileIdList, String userId, String fileId, Integer delFlag) {
        fileIdList.add(fileId);
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFilePid(fileId);
        query.setDelFlag(delFlag);
        query.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        List<FileInfo> fileInfoList = this.fileInfoMapper.selectList(query);
        for (FileInfo fileInfo : fileInfoList) {
            findAllSubFolderFileIdList(fileIdList, userId, fileInfo.getFileId(), delFlag);
        }
    }
}