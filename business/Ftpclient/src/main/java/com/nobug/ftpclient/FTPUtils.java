package com.nobug.ftpclient;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

public class FTPUtils {

    //ftp服务器地址
    private String hostname = "";
    //ftp服务器端口号默认为21
    private Integer port = 21;
    //ftp登录账号
    private String username = "";
    //ftp登录密码
    private String password = "";
    //超时时间
    public int timeOut = 5;
    //被动模式开关 如果不开被动模式 有防火墙 可能会上传失败， 但被动模式需要ftp支持
    public boolean enterLocalPassiveMode = false;

    private FTPClient ftpClient = null;

    private static FTPUtils mFTPUtils = null;


    private FTPUtils() {

    }

    public static FTPUtils getInstance() {
        if (mFTPUtils == null) {
            synchronized (FTPUtils.class) {
                if (mFTPUtils == null) {
                    mFTPUtils = new FTPUtils();
                }
            }
        }

        return mFTPUtils;
    }


    /**
     * 初始化配置  全局只需初始化一次
     * @param hostname  ftp服务器地址
     * @param port ftp服务器端口号默认为21
     * @param username ftp登录账号
     * @param password ftp登录密码
     */
    public void initFtpClient(String hostname, int port, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;

        //初始化ftpclient对象
        ftpClient = new FTPClient();
        //设置超时时间以毫秒为单位使用时，从数据连接读。
        ftpClient.setDefaultTimeout(timeOut * 1000);
        ftpClient.setConnectTimeout(timeOut * 1000);
        ftpClient.setDataTimeout(timeOut * 1000);
        ftpClient.setControlEncoding("utf-8");
    }




    /**
     * 连接并登陆ftp
     * @return
     */
    private boolean connectFtp(){
        boolean connectResult = false;
        try {
            Log.e("FTP", "开始连接...FTP服务器...");
            ftpClient.connect(hostname, port); //连接ftp服务器
            Log.e("FTP", "...成功连接FTP服务器...");
            //是否开启被动模式        Log.e("FTP", "...成功连接FTP服务器...");
            if (enterLocalPassiveMode) {
                ftpClient.setRemoteVerificationEnabled(false);
                ftpClient.enterLocalPassiveMode();
            }
            Log.e("FTP", "...开始登录FTP服务器...");
            ftpClient.login(username, password); //登录 ftp服务器
            Log.e("FTP", "...成功登录FTP服务器...");
            int replyCode = ftpClient.getReplyCode(); //是否成功登录服务器
            if (FTPReply.isPositiveCompletion(replyCode)) {
                connectResult = true;
                Log.e("FTP","登录...FTP服务器...成功:" + this.hostname + ":" + this.port);
            } else {
                ftpClient.disconnect();
                connectResult = false;
                Log.e("--------->","登录...FTP服务器...失败: " + this.hostname + ":" + this.port+ "");
            }
        } catch (MalformedURLException e) {
            Log.e(e.getMessage(), e+"");
        } catch (IOException e) {
            Log.e(e.getMessage(), e+"");
        }
        return connectResult;
    }

    // 实现下载文件功能，可实现断点下载
    public synchronized boolean downloadFile(String localPath, String serverPath) throws Exception {
        // 先判断服务器文件是否存在
        FTPFile[] files = ftpClient.listFiles(serverPath);
        if (files.length == 0) {
            System.out.println("服务器文件不存在");
            return false;
        }
        System.out.println("远程文件存在,名字为：" + files[0].getName());
        localPath = localPath + files[0].getName();
        // 接着判断下载的文件是否能断点下载
        long serverSize = files[0].getSize(); // 获取远程文件的长度
        File localFile = new File(localPath);
        long localSize = 0;
        if (localFile.exists()) {
            localSize = localFile.length(); // 如果本地文件存在，获取本地文件的长度
            if (localSize == serverSize) {
                System.out.println("文件已经下载完了");
                File file = new File(localPath);
                file.delete();
                System.out.println("本地文件存在，删除成功，开始重新下载");
                return false;
            }
        }
        // 进度
        long step = serverSize / 100;
        long process = 0;
        long currentSize = 0;
        // 开始准备下载文件
        ftpClient.enterLocalActiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        OutputStream out = new FileOutputStream(localFile, true);
        ftpClient.setRestartOffset(localSize);
        InputStream input = ftpClient.retrieveFileStream(serverPath);
        byte[] b = new byte[1024];
        int length = 0;
        while ((length = input.read(b)) != -1) {
            out.write(b, 0, length);
            currentSize = currentSize + length;
            if (currentSize / step != process) {
                process = currentSize / step;
                if (process % 10 == 0) {
                    System.out.println("下载进度：" + process);
                }
            }
        }
        out.flush();
        out.close();
        input.close();
        // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
        if (ftpClient.completePendingCommand()) {
            System.out.println("文件下载成功");
            return true;
        } else {
            System.out.println("文件下载失败");
            return false;
        }
    }

    /**
     * 上传文件
     *
     * @param ftpSavePath     ftp服务保存地址  (不带文件名)
     * @param ftpSaveFileName 上传到ftp的文件名
     * @param originFile      待上传文件
     * @return
     */
    public boolean uploadFile(String ftpSavePath, String ftpSaveFileName, File originFile) {
        boolean flag = false;
        try {
            FileInputStream inputStream = new FileInputStream(originFile);
            flag = uploadFile(ftpSavePath, ftpSaveFileName, inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("FTP", e.getMessage() + "  " + e);
        }
        return flag;
    }



    /**
     * 上传文件
     *
     * @param ftpSavePath     ftp服务保存地址  (不带文件名)
     * @param ftpSaveFileName 上传到ftp的文件名
     * @param originFileName  待上传文件的名称（绝对地址） *
     * @return
     */
    public boolean uploadFile(String ftpSavePath, String ftpSaveFileName, String originFileName) {
        boolean flag = false;

        try {
            FileInputStream inputStream = new FileInputStream(originFileName);
            flag = uploadFile(ftpSavePath, ftpSaveFileName, inputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("------------>", e.getMessage() + "  " + e);
        }
        return flag;
    }

    /**
     * 上传文件(直接读取输入流形式)
     *
     * @param ftpSavePath    ftp服务保存地址
     * @param ftpSaveFileName    上传到ftp的文件名
     * @param inputStream 输入文件流
     * @return
     */
    public boolean uploadFile(String ftpSavePath, String ftpSaveFileName, InputStream inputStream) {
        boolean uploadResult = false;
        try {
            if (!ftpClient.isConnected() && !connectFtp()) {
                return false; // 连接FTP服务器失败
            }
            //第一次进来,将上传路径设置成相对路径
            if (ftpSavePath.startsWith("/")) {
                ftpSavePath = ftpSavePath.substring(1);
            }
            Log.e("FTP","上传文件的路径 :" + ftpSavePath);
            Log.e("FTP", "上传文件名 :" + ftpSaveFileName);
            Log.e( "FTP", "开始上传文件...");
            //设置文件类型,图片为二进制
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //创建文件路径
            if (!CreateDirecroty(ftpSavePath)) {
                return false;
            }
            uploadResult = ftpClient.storeFile(new String(ftpSaveFileName.getBytes("GBK"), "iso-8859-1"), inputStream);
            inputStream.close();
            ftpClient.logout();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    Log.e(e.getMessage(), e+"");
                }
            }
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(e.getMessage(), e+"");
                }
            }
            Log.e("FTP","上传文件结束...结果 :" + (uploadResult ? "成功" : "失败 "));
        }
        return uploadResult;
    }


    //改变目录路径
    public boolean changeWorkingDirectory(String directory) {
        boolean flag = true;
        try {
            flag = ftpClient.changeWorkingDirectory(directory);
            if (!flag) {
                Log.e( "FTP", "所在的目录 : " + ftpClient.printWorkingDirectory() + " 进入下一级 " + directory + " 目录失败");
            } else {
                Log.e("FTP","进入目录成功，当前所在目录 :" + ftpClient.printWorkingDirectory());
            }
        } catch (IOException ioe) {
            Log.e(ioe.getMessage(), ioe+"");
        }
        return flag;
    }

    //创建多层目录文件，如果有ftp服务器已存在该文件，则不创建，如果无，则创建
    public boolean CreateDirecroty(String remote) throws IOException {
        boolean success = true;
        String directory = remote + "/";
        // 如果远程目录不存在，则递归创建远程服务器目录
        if (!directory.equalsIgnoreCase("/") && !changeWorkingDirectory(new String(directory))) {
            int start = 0;
            int end = 0;
            if (directory.startsWith("/")) {
                start = 1;
            } else {
                start = 0;
            }
            //从第一个"/"索引之后开始得到下一个"/"的索引
            end = directory.indexOf("/", start);
            while (true) {
                Log.e("FTP","所在的目录 :" + ftpClient.printWorkingDirectory());
                String subDirectory = new String(remote.substring(start, end).getBytes("GBK"), "iso-8859-1");
                if (!existFile(subDirectory)) {
                    if (makeDirectory(subDirectory)) {
                        if (!changeWorkingDirectory(subDirectory)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    if (!changeWorkingDirectory(subDirectory)) {
                        return false;
                    }
                }

                start = end + 1;
                end = directory.indexOf("/", start);
                // 检查所有目录是否创建完毕
                if (end <= start) {
                    break;
                }
            }
        }
        return success;
    }

    //判断ftp服务器文件是否存在
    public boolean existFile(String path) throws IOException {
        boolean flag = false;
        FTPFile[] ftpFileArr = ftpClient.listFiles(path);
        if (ftpFileArr.length > 0) {
            flag = true;
        }
        return flag;
    }

    //创建目录
    public boolean makeDirectory(String dir) {
        boolean flag = true;
        try {
            flag = ftpClient.makeDirectory(dir);
            if (!flag) {
                Log.e("FTP","所在的目录 : " + ftpClient.printWorkingDirectory() + " 创建下一级 " + dir + " 目录失败 ");
            } else {
                Log.e("FTP","所在的目录 : " + ftpClient.printWorkingDirectory() + " 创建下一级 " + dir + " 目录成功 ");
            }
        } catch (Exception e) {
            Log.e(e.getMessage(), e+"");
        }
        return flag;
    }

    /**
     * 下载文件 *
     *
     * @param pathname  FTP服务器文件目录 *
     * @param filename  文件名称 *
     * @param localpath 下载后的文件路径 *
     * @return
     */
    public boolean downloadFile(String pathname, String filename, String localpath) {
        boolean flag = false;
        OutputStream os = null;
        try {
            //第一次进来,将上传路径设置成相对路径
            if (pathname.startsWith("/")) {
                pathname = pathname.substring(1);
            }
            connectFtp();
            ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
            //切换FTP目录
            changeWorkingDirectory(pathname);
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for (FTPFile file : ftpFiles) {
                if (filename.equalsIgnoreCase(file.getName())) {
                    File localFile = new File(localpath + "/" + file.getName());
                    os = new FileOutputStream(localFile);
                    ftpClient.retrieveFile(file.getName(), os);
                    os.close();
                }
            }
            ftpClient.logout();
            flag = true;
        } catch (Exception e) {
            Log.e(e.getMessage(), e+"");
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    Log.e(e.getMessage(), e+"");
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(e.getMessage(), e+"");
                }
            }
        }
        return flag;
    }

    /**
     * 删除文件 *(未测试)
     *
     * @param pathname FTP服务器保存目录 *
     * @param filename 要删除的文件名称 *
     * @return
     */
    public boolean deleteFile(String pathname, String filename) {
        boolean flag = false;
        try {
            //第一次进来,将上传路径设置成相对路径
            if (pathname.startsWith("/")) {
                pathname = pathname.substring(1);
            }
            connectFtp();
            //切换FTP目录
            changeWorkingDirectory(pathname);
            ftpClient.dele(filename);
            ftpClient.logout();
            flag = true;
        } catch (Exception e) {
            Log.e("FTP","删除文件失败 ");
            Log.e("FTP",e.getMessage());
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    Log.e(e.getMessage(), e+"");
                }
            }
        }
        return flag;
    }

    /**
     * 获取文件的输入流
     *
     * @param dir      ftp定义的存储路径 例如 /ftpFile/images
     * @param filename 上传的文件名
     * @return
     * @throws Exception
     *//*
    public InputStream getInputStream(String dir, String filename) {
        byte[] bytes = null;
        String path = dir + filename;
        InputStream in = null;
        try {
            connectFtp();
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            // 转到指定下载目录
            if (path != null) {
                //验证是否有该文件夹，有就转到，没有创建后转到该目录下
                changeWorkingDirectory(path);// 转到指定目录下
            }
            // 不需要遍历，改为直接用文件名取
            String remoteAbsoluteFile = toFtpFilename(path);
            // 下载文件
            ftpClient.setBufferSize(1024 * 1024);
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
            in = ftpClient.retrieveFileStream(remoteAbsoluteFile);
            *//*
     * bytes = input2byte(in); System.out.println("下载成功!" + bytes.length); //
     * in.read(bytes); in.close();
     *//*
        } catch (SocketException e) {
//            e.printStackTrace();
            Log.e(e.getMessage(), e+"");
        } catch (IOException e) {
//            e.printStackTrace();
            Log.e(e.getMessage(), e+"");
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e(e.getMessage(), e+"");
        }
        return in;
    }*/

    /**
     * 文件转成 byte[]
     *
     * @param inStream
     * @return
     * @throws IOException
     * @see [类、类#方法、类#成员]
     */
    public static byte[] input2byte(InputStream inStream) throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[inStream.available()];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        swapStream.close();
        return in2b;
    }

    /**
     * 转化输出的编码
     */
    private static String toFtpFilename(String fileName) throws Exception {
        return new String(fileName.getBytes("UTF-8"), "ISO8859-1");
    }


}