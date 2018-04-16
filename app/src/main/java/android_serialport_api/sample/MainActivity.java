package android_serialport_api.sample;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.zyapi.CommonApi;

import com.by100.util.AppConfig;
import com.by100.util.CopyFileToSD;
import com.by100.util.NationDeal;
import com.ivsign.android.IDCReader.IDCReaderSDK;

import java.io.FileInputStream;
import java.io.IOException;


public class MainActivity extends SerialPortActivity {

    byte[] cmd_SAM = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x12, (byte) 0xFF, (byte) 0xEE  };
    byte[] cmd_find  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x01, 0x22  };
    byte[] cmd_selt  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x02, 0x21  };
    byte[] cmd_read  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x30, 0x01, 0x32 };
    byte[] cmd_sleep  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x02, 0x00, 0x02};
    byte[] cmd_weak  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x02, 0x01, 0x03 };
    byte[] recData = new byte[1500];
    private String[] decodeInfo = new String[10];

    private SharedPreferences prefs;
    private MediaPlayer player;
    private CommonApi mCommonApi;
    private boolean isOpen;
    private boolean isRun;
    private int Readflage = -99;

    private Button readCard;
    private TextView ett;
    private ImageView image;
    private boolean isPlay;
    private int mComFd = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readCard = findViewById(R.id.button3);
        image = findViewById(R.id.imageView);
        ett = findViewById(R.id.textView);
        init();

    }

    @Override
    protected void onDataReceived(byte[] buffer, int size) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        isPlay = prefs.getBoolean("checkbox",true);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        isRun = false;
//		if(mComFd>0){
//		    mCommonApi.setGpioDir(9,1);
//		    mCommonApi.setGpioOut(9,0);
        mCommonApi.setGpioDir(53,1);
        mCommonApi.setGpioOut(53,0);
//			Toast.makeText(getApplicationContext(), "退出", 0).show();
//			mCommonApi.setGpioDir(78,1);
//			mCommonApi.setGpioOut(78,0);
        mCommonApi.closeCom(mComFd);
//		}
        super.onDestroy();
    }

    private void init() {
        CopyFileToSD cFileToSD = new CopyFileToSD();
        cFileToSD.initDB(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        player = MediaPlayer.create(this, R.raw.success);

        mCommonApi=new CommonApi();
        mCommonApi.setGpioDir(53,0);
        mCommonApi.getGpioIn(53);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int  ret2 = mCommonApi.setGpioOut(53,1);
                if(ret2 == 0){
                    Toast.makeText(MainActivity.this, "设置成功" , Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "设置失败" , Toast.LENGTH_SHORT).show();
                }
            }
        }, 1000);

        readCard.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                isOpen = !isOpen;
                if(isOpen){
                    readCard.setText("停止");
                }else{
                    readCard.setText("读卡");
                }
            }
        });
    }


    private class ThreadRun implements Runnable{
        @Override
        public void run() {
            while(isRun){
                try{
                    Thread.sleep(1000);
                    if(isOpen)
                        ReadCard();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void ReadCard() {
        try {
            if ((mInputStream == null) || (mInputStream == null)) {
                Readflage = -2;// 连接异常
                return;
            }
            mOutputStream.write(cmd_find);
            Thread.sleep(200);
            int datalen = mInputStream.read(recData);
            if (recData[9] == -97) {
                mOutputStream.write(cmd_selt);
                Thread.sleep(200);
                datalen = mInputStream.read(recData);
                if (recData[9] == -112) {
                    mOutputStream.write(cmd_read);
                    Thread.sleep(1000);
                    byte[] tempData = new byte[1500];
                    if (mInputStream.available() > 0) {
                        datalen = mInputStream.read(tempData);
                    } else {
                        Thread.sleep(500);
                        if (mInputStream.available() > 0) {
                            datalen = mInputStream.read(tempData);
                        }
                    }
                    int flag = 0;
                    if (datalen < 1294) {
                        for (int i = 0; i < datalen; i++, flag++) {
                            recData[flag] = tempData[i];
                        }
                        Thread.sleep(1000);
                        if (mInputStream.available() > 0) {
                            datalen = mInputStream.read(tempData);
                        } else {
                            Thread.sleep(500);
                            if (mInputStream.available() > 0) {
                                datalen = mInputStream.read(tempData);
                            }
                        }
                        for (int i = 0; i < datalen; i++, flag++) {
                            recData[flag] = tempData[i];
                        }

                    } else {
                        for (int i = 0; i < datalen; i++, flag++) {
                            recData[flag] = tempData[i];
                        }
                    }
                    tempData = null;
                    if(flag == 1295){
                        if (recData[9] == -112) {

                            byte[] dataBuf = new byte[256];
                            for (int i = 0; i < 256; i++) {
                                dataBuf[i] = recData[14 + i];
                            }
                            String TmpStr = new String(dataBuf, "UTF16");
                            TmpStr = new String(TmpStr.getBytes("UTF-8"));
                            decodeInfo[0] = TmpStr.substring(0, 15);
                            decodeInfo[1] = TmpStr.substring(15, 16);
                            decodeInfo[2] = TmpStr.substring(16, 18);
                            decodeInfo[3] = TmpStr.substring(18, 26);
                            decodeInfo[4] = TmpStr.substring(26, 61);
                            decodeInfo[5] = TmpStr.substring(61, 79);
                            decodeInfo[6] = TmpStr.substring(79, 94);
                            decodeInfo[7] = TmpStr.substring(94, 102);
                            decodeInfo[8] = TmpStr.substring(102, 110);
                            decodeInfo[9] = TmpStr.substring(110, 128);
                            if (decodeInfo[1].equals("1"))
                                decodeInfo[1] = "男";
                            else
                                decodeInfo[1] = "女";
                            try {
                                int code = Integer.parseInt(decodeInfo[2]
                                        .toString());
                                decodeInfo[2] = NationDeal.decodeNation(code);
                            } catch (Exception e) {
                                decodeInfo[2] = "";
                            }

                            // 照片解码
                            try {
                                int ret = IDCReaderSDK.Init();
                                if (ret == 0){
                                    byte[] datawlt = new byte[1384];
                                    byte[] byLicData = { (byte) 0x05,
                                            (byte) 0x00, (byte) 0x01,
                                            (byte) 0x00, (byte) 0x5B,
                                            (byte) 0x03, (byte) 0x33,
                                            (byte) 0x01, (byte) 0x5A,
                                            (byte) 0xB3, (byte) 0x1E,
                                            (byte) 0x00 };
                                    for (int i = 0; i < 1295; i++) {
                                        datawlt[i] = recData[i];
                                    }
                                    int t = IDCReaderSDK.unpack(datawlt,
                                            byLicData);
                                    if (t == 1) {
                                        Readflage = 1;// 读卡成功
                                    } else {
                                        Readflage = 6;// 照片解码异常
                                    }
                                } else {
                                    Readflage = 6;// 照片解码异常
                                }
                            } catch (Exception e) {
                                Readflage = 6;// 照片解码异常
                            }
                            handler.sendEmptyMessage(0);
                        } else {
                            Readflage = -5;// 读卡失败！
                        }
                    } else {
                        Readflage = -5;// 读卡失败
                    }
                } else {
                    Readflage = -4;// 选卡失败
                }
            } else {
                Readflage = -3;// 寻卡失败
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Readflage = -99;// 读取数据异常
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            Readflage = -99;// 读取数据异常
        }
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if(msg.what!=0){
                return;
            }
            try {
                if(Readflage > 0) {
                    ett.setText("姓名：" + decodeInfo[0] + "\n" + "性别："
                            + decodeInfo[1] + "\n" + "民族：" + decodeInfo[2]
                            + "\n" + "出生日期：" + decodeInfo[3] + "\n" + "地址："
                            + decodeInfo[4] + "\n" + "身份号码：" + decodeInfo[5]
                            + "\n" + "签发机关：" + decodeInfo[6] + "\n" + "有效期限："
                            + decodeInfo[7] + "-" + decodeInfo[8] + "\n"
                            + decodeInfo[9] + "\n");
                    if (Readflage == 1) {
                        FileInputStream fis = new FileInputStream(
                                Environment.getExternalStorageDirectory()
                                        + "/wltlib/zp.bmp");
                        Bitmap bmp = BitmapFactory.decodeStream(fis);
                        fis.close();
                        image.setImageBitmap(bmp);
                    } else {
                        ett.append("照片解码失败，请检查路径"
                                + AppConfig.RootFile);
                        image.setImageBitmap(BitmapFactory.decodeResource(
                                getResources(), R.drawable.ic_launcher_background));
                    }
                    if(isPlay)
                        player.start();
                }else{
                    image.setImageBitmap(BitmapFactory.decodeResource(
                            getResources(), R.drawable.ic_launcher_background));
                    if (Readflage == -2) {
                        ett.setText("连接异常");
                    }
                    if (Readflage == -3) {
                        ett.setText("无卡或卡片已读过");
                    }
                    if (Readflage == -4) {
                        ett.setText("无卡或卡片已读过");
                    }
                    if (Readflage == -5) {
                        ett.setText("读卡失败");
                    }
                    if (Readflage == -99) {
                        ett.setText("操作异常");
                    }
                }
                Thread.sleep(100);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                ett.setText("读取数据异常！");
                image.setImageBitmap(BitmapFactory.decodeResource(
                        getResources(), R.drawable.ic_launcher_background));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                ett.setText("读取数据异常！");
                image.setImageBitmap(BitmapFactory.decodeResource(
                        getResources(), R.drawable.ic_launcher_background));
            }
        }
    };

}
