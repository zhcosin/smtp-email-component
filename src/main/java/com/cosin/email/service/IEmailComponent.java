package com.cosin.email.service;

import com.cosin.email.conf.EmailConf;
import com.cosin.email.data.EmailDataInfo;

public interface IEmailComponent {
	// 初始化
	void init(EmailConf conf) throws RuntimeException;
	
	// 发送邮件
	void send(EmailDataInfo emailData) throws RuntimeException;
}
