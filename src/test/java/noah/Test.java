package noah;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

/**
 * Test
 * Created by noah on 17-4-25.
 */
public class Test {

	@org.junit.Test
	public void testString() {
		String test = "test,a";
		System.out.println(test.substring(0, test.indexOf(",")));
	}

	/**
	 * ftp.host= 10.142.164.74
	 * ftp.user=  YH_FTP
	 * ftp.passwd= ftp123$%^
	 */
	@org.junit.Test
	public void testFTP() {
		FTPClient ftpClient = new FTPClient();
		InputStream fis = null;
		try {
			ftpClient.connect("10.142.164.74", 21);
			ftpClient.login("YH_FTP", "ftp123$%^");
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				ftpClient.disconnect();
				return;
			}
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftpClient.setControlEncoding("UTF-8");
			ftpClient.changeWorkingDirectory("/disk/ftp/YH_FTP/test");


			System.out.println(ftpClient.printWorkingDirectory());
			fis = new FileInputStream("/home/noah/templates/unsm/smslogs/MOB-UPAY-MIN");
			boolean testFTP = ftpClient.storeFile("testFTP", fis);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				IOUtils.closeQuietly(fis);
				if (!ftpClient.isConnected()) {
					ftpClient.logout();
				}
				ftpClient.disconnect();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static boolean upload(String ftpUrl, String userName, int port,
	                             String password, String directory, String srcFileName, String destName) throws IOException {
		FTPClient ftpClient = new FTPClient();
		FileInputStream fis = null;
		boolean result = false;
		try {
			ftpClient.connect(ftpUrl, port);
			ftpClient.login(userName, password);
			ftpClient.enterLocalPassiveMode();//主动模式
			File srcFile = new File(srcFileName);
			fis = new FileInputStream(srcFile);
			// 设置上传目录
			ftpClient.changeWorkingDirectory(directory);
			ftpClient.setBufferSize(1024);
			ftpClient.setControlEncoding("gbk");
			// 设置文件类型（二进制）
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			result = ftpClient.storeFile(destName, fis);
			return result;
		} catch (NumberFormatException e) {
			System.out.println("FTP端口配置错误:不是数字:");
			throw e;
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException();
		} catch (IOException e) {
			throw new IOException(e);
		} finally {
			IOUtils.closeQuietly(fis);
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				throw new RuntimeException("关闭FTP连接发生异常！", e);
			}
		}
	}
}
