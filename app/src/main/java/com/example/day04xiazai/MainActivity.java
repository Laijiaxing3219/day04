package com.example.day04xiazai;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.greenrobot.eventbus.EventBus;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static String url="http://cdn.banmi.com/banmiapp/apk/banmi_330.apk";
    private static int count=0;
    /**
     * 40 %
     */
    private CircleProgressBar mCpb;
    /**
     * 开始下载
     */
    private Button mBtn;

    private boolean mIsDownload = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    private void initView() {
        mCpb = (CircleProgressBar) findViewById(R.id.cpb);
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn:
                okDownload(url);
                download();
                break;
        }
    }


    public static void okDownload(String url){
        final String savaFile = "/storage/emulated/legacy/a.apk";
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        //异步调用,不用再新建线程了
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                //第一个0表示失败，第二个0表示进度，第三个0表示最大进度，失败都为0
                EventBus.getDefault().post(new MsgE(0,0,0));

            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();
                long max = response.body().contentLength();//得到文件的大小
                Log.i("TAG", "onResponse: 1111111111111111111111111111");
                savaFile(savaFile,inputStream,max);//保存文件到本地
            }
        });
    }

    private static void savaFile(String savaFile, InputStream is, long max) {
        Log.i("TAG", "savaFile: 22222222");
        int len = 0;
        byte[] buff = new byte[4096];//每次读取4k
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(savaFile);
            while ((len = is.read(buff)) != -1){
                Log.i("TAG", "savaFile:333333333333333 ");
                fos.write(buff,0,len);//把读取的数据，写入本地文件中，作为保存
                count = count+len;//累加读取的数据
                //2表示更新进度条的进度
                Log.i("111", "savaFile: max->"+max+",count->"+count);
            }
            //3表示下载完成，Toast提示
            Log.i("TAG", "savaFile: 成功");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                //读取写入完数据后，关闭流
                if(is != null)
                    is.close();
                if(fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void download() {
        if (!mIsDownload){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mIsDownload = true;
                    for (int i = 0; i < 101; i++) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        mCpb.setProgresss(i);
                        if (i == 100){
                            mIsDownload = false;
                        }
                    }
                }
            }).start();

        }
    }


}
