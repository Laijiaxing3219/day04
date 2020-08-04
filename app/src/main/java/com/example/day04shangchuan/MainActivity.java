package com.example.day04shangchuan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String mUpFileUrl = "http://yun918.cn/study/public/file_upload.php";
    /**
     * HTTPUrlConnection上传文件
     */
    private Button mBtnUpload;
    private ProgressBar mPb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);//注册
        initView();
        checkPermission();//处理动态权限

    }
    @Subscribe(threadMode = ThreadMode.MAIN)//设置为主线程
    public void getMsg(ProMsg proMsg){
        if(proMsg.getType() == 1){//表示设置最大进度
            mPb.setMax(proMsg.getMax());
        }else if (proMsg.getType() == 2){
            mPb.setProgress(proMsg.getProgress());//更新进度条
        }
    }

    private void checkPermission() {
        int i = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (i != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat
                    .requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    private void initView() {
        mBtnUpload = (Button) findViewById(R.id.btn_upload);
        mBtnUpload.setOnClickListener(this);
        mPb = (ProgressBar) findViewById(R.id.pb);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_upload:
                HTTPUrlConnectionUpload();
                break;
        }
    }

    private void HTTPUrlConnectionUpload() {
        Executor e = null;
        ExecutorService s = null;
        ThreadPoolExecutor p = null;


        //准备文件
        final File file = new File("/storage/emulated/0/mm.png");
        if (file.exists()) {//存在，则上传

//            new Thread() { //启动线程，执行耗时的上传功能
//                @Override
//                public void run() {
//                    super.run();
//                    uploadForm(null, "file", file, file.getName(), mUpFileUrl);
//                }
//            }.start();

            //创建线程池  进行线程优化处理
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    5,//核心线程数量,核心池的大小
                    20,//线程池最大线程数
                    30,//表示线程没有任务执行时最多保持多久时间会终止
                    TimeUnit.SECONDS,//时间单位
                    new LinkedBlockingQueue<Runnable>(),//任务队列,用来存储等待执行的任务
                    Executors.defaultThreadFactory(),//线程工厂,如何去创建线程的
                    new ThreadPoolExecutor.AbortPolicy()//异常的捕捉器
            );
            //获得要执行的线程任务，会由线程此来管理
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    uploadForm(null, "file", file, file.getName(), mUpFileUrl);
                }
            });
        } else {
            Toast.makeText(this, "文件不存在，请检查", Toast.LENGTH_SHORT).show();
        }
    }

    // 分割符,自己定义即可
    private static final String BOUNDARY = "----1111WebKitFormBoundaryT1HoybnYeFOGFlBRqwe";

    //原始的http上传
    public void uploadForm(Map<String, String> params, String fileFormName, File uploadFile, String newFileName, String urlStr) {

        try {
            if (newFileName == null || newFileName.trim().equals("")) {
                newFileName = uploadFile.getName();
            }

            StringBuilder sb = new StringBuilder();//比StringBuffer的性能更高，更快  因为是 线程不安全
            /**
             * 普通的表单数据
             */
            if (params != null) {
                for (String key : params.keySet()) {
                    sb.append("--" + BOUNDARY + "\r\n");
                    sb.append("Content-Disposition: form-data; name=\"" + key + "\"" + "\r\n");
                    sb.append("\r\n");
                    sb.append(params.get(key) + "\r\n");
                }
            }

            /**
             * 上传文件的头
             */
            sb.append("--" + BOUNDARY + "\r\n");
            sb.append("Content-Disposition: form-data; name=\"" + fileFormName + "\"; filename=\"" + newFileName + "\""
                    + "\r\n");
            sb.append("Content-Type: application/octet-stream" + "\r\n");// 如果服务器端有文件类型的校验，必须明确指定ContentType
            sb.append("\r\n");

            byte[] headerInfo = sb.toString().getBytes("UTF-8");
            byte[] endInfo = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");

            //开始和服务器的连接
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            // 设置传输内容的格式，以及长度
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.setRequestProperty("Content-Length", String.valueOf(headerInfo.length + uploadFile.length() + endInfo.length));
            conn.setDoOutput(true);

            OutputStream out = conn.getOutputStream();//得到服务器的输出流
            InputStream in = new FileInputStream(uploadFile);//创建文件的输入流，获得上传文件信息，写入到out 输出流，，数据流

            //写入的文件长度
            int count = 0;  //当前上传的大小
            int available = in.available();//文件的总长度
            EventBus.getDefault().post(new ProMsg(1,0,available));//把文件的长度，和进度条的最大值同步一致
            // 写入头部 （包含了普通的参数，以及文件的标示等）
            out.write(headerInfo);
            // 写入文件
            byte[] buf = new byte[1024];//1k  每次上传1k
            int len;
            while ((len = in.read(buf)) != -1) {//循环读取本地的文件的内容
                out.write(buf, 0, len);//读取一次信息，向服务器写入一次信息
                count += len;//上传文件的大小的累计
                int progress = (int) (((float)count / available) * 100);//0.111算出上传的百分比，和进度条的总长度相乘，得到进度中的进度值
                Log.d("111", "上传进度: " + progress + " %");
//                updateProgress(count);//把上传进度设置给进度条
                //通过EventBus实现进度条的更新
//                EventBus.getDefault().post(count);
                EventBus.getDefault().post(new ProMsg(2,count,available));//通过EventBus实现进度条的更新
                Thread.sleep(1000);//没1秒 上传一次，一部分 1k
            }
            // 写入尾部
            out.write(endInfo);
            in.close();//上传完成，关闭输入流
            out.close();//关闭输出流
            //上传后，获得服务器响应的结果，判断是否成功
            if (conn.getResponseCode() == 200) {
                Log.i("111", "文件上传成功");
                String s = stream2String(conn.getInputStream());//得到上传成功后，服务器返回的信息
                Log.d("111", "uploadForm--上传成功: " + s);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String stream2String(InputStream is) {
        int len;
        byte[] bytes = new byte[1024];
        StringBuffer sb = new StringBuffer();
        try {
            while ((len = is.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, len));
            }

            is.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }


}
