package com.easypan.controller;

import java.io.IOException;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.CreateImageCode;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.enums.VerifyRegexEnum;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.exception.BusinessException;
import com.easypan.service.EmailCodeService;
import com.easypan.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 用户信息 Controller
 */
@RestController("userInfoController")
public class AccountController extends ABaseController{
	@Resource
	private UserInfoService userInfoService;
	@Resource
	private EmailCodeService emailCodeService;

	@RequestMapping("/checkCode")
	public void checkCode(HttpServletResponse response, HttpSession session,Integer type) throws
			IOException {
		CreateImageCode vCode = new CreateImageCode(130,30,5,10);
		response.setHeader("Pragma","no-cache");
		response.setHeader("Cache-Control","no-cache");
		response.setDateHeader("Expires",0);
		response.setContentType("image/jpeg");
		String code = vCode.getCode();
		if (type == null || type == 0) {
			session.setAttribute(Constants.CHECK_CODE_KEY,code);
		} else {
			session.setAttribute(Constants.CHECK_CODE_KEY_EMIL,code);
		}
		vCode.write(response.getOutputStream());
	}

	/**
	 * 邮箱验证码
	 * @param session
	 * @param email
	 * @param checkCode
	 * @param type
	 * @return
	 */
	@RequestMapping("/sendEmailCode")
	@GlobalInterceptor
	public ResponseVO sendEmailCode(HttpSession session,
									@VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL,max = 150) String email,
									@VerifyParam(required = true) String checkCode,
									@VerifyParam(required = true) Integer type){
		try {
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY_EMIL))) {
				throw new BusinessException("图片验证码不正确");
			}
			emailCodeService.sendEmailCode(email,type);
			return getSuccessResponseVO(null);
		} finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY_EMIL);
		}

	}

	@RequestMapping("/register")
	@GlobalInterceptor
	public ResponseVO register(HttpSession session,
							   @VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL,max = 150) String email,
							   @VerifyParam(required = true) String nickName,
							   @VerifyParam(required = true,regex = VerifyRegexEnum.PASSWORD,min=8,max=10) String password,
							   @VerifyParam(required = true) String checkCode,
							   @VerifyParam(required = true) String emailCode){
		try {
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
				throw new BusinessException("图片验证码不正确");
			}
			userInfoService.register(email,nickName,password,emailCode);
			emailCodeService.checkCode(emailCode,checkCode);
			return getSuccessResponseVO(null);
		} finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}

	}
	@RequestMapping("/login")
	@GlobalInterceptor
	public ResponseVO login(HttpSession session,
							   @VerifyParam(required = true) String email,
							   @VerifyParam(required = true) String password,
							   @VerifyParam(required = true) String checkCode){
		try {
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
				throw new BusinessException("图片验证码不正确");
			}
			SessionWebUserDto sessionWebUserDto =userInfoService.login(email,password);
			session.setAttribute(Constants.SESSION_KEY,sessionWebUserDto);

			return getSuccessResponseVO(sessionWebUserDto);
		} finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}

	}
}