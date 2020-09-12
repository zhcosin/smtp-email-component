package com.cosin.email.service;

import java.security.InvalidParameterException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.nlab.smtp.pool.SmtpConnectionPool;
import org.nlab.smtp.transport.connection.ClosableSmtpConnection;
import org.nlab.smtp.transport.factory.SmtpConnectionFactories;
import org.nlab.smtp.transport.factory.SmtpConnectionFactory;

import com.cosin.email.conf.EmailConf;
import com.cosin.email.data.EmailDataInfo;

public class EmailComponent implements IEmailComponent {
	
	private EmailConf conf;
	
	private Session smtpSession = null;
	private SmtpConnectionFactory smtpfactory = null;
	private SmtpConnectionPool smtpConnectionPool = null;

	public void init(EmailConf conf) throws RuntimeException {
		try {
			Properties props = new Properties();
	        //服务器名称
	        props.setProperty("mail.host",conf.getHost());
	        //传输协议
	        props.setProperty("mail.transport.protocol",conf.getProtocol());
	        //设置是否要验证服务器用户名和密码
	        props.setProperty("mail.smtp.auth","true");
	        // 服务器端口号
	        props.setProperty("mail.smtp.port", conf.getPort());
	        //创建一个session对象
	        final EmailConf theEmailConf = conf;
	        this.smtpSession = Session.getDefaultInstance(props, new Authenticator() {
	            @Override
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication(theEmailConf.getSenderAddress(),theEmailConf.getSmtpServer());
	            }
	        });
			
			this.smtpfactory = SmtpConnectionFactories.newSmtpFactory(this.smtpSession);
			
			this.conf = conf;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void send(EmailDataInfo emailData) throws RuntimeException {
		if (emailData == null)
			throw new InvalidParameterException("email data info is null when send.");
		
		try {
			try (ClosableSmtpConnection transport = this.smtpConnectionPool.borrowObject()) {
				 //创建邮件对象
                MimeMessage mimeMessage = new MimeMessage(smtpConnectionPool.getSession());

                //邮件发送人
                mimeMessage.setFrom(new InternetAddress(emailData.getSenderEmail()));

                //邮件接收人
                mimeMessage.setRecipient(Message.RecipientType.TO,new InternetAddress(emailData.getReceiverEmail()));

                //邮件标题
                mimeMessage.setSubject(emailData.getTopic());

                //邮件内容
                mimeMessage.setContent(emailData.getContent(),this.conf.getCharacter());

                //发送邮件
                transport.sendMessage(mimeMessage,mimeMessage.getAllRecipients());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
