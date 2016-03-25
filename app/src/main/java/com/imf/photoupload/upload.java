package com.imf.photoupload;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class upload extends AppCompatActivity {

    String webip="http://140.113.2.218/~p0213453/IMF/upload.php";
    String photodir=getSdcardPath()+"demo";
    ImageView iv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);



        String path = photodir+"/now.jpg";

        iv.setImageDrawable(Drawable.createFromPath(path));

        Button backbutton = (Button) findViewById(R.id.back);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    this.finalize();//回到頂層 activity
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });

        Button upbutton = (Button) findViewById(R.id.up);
        upbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            up();
                try {
                    this.finalize();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });



    }


    public int up(){
        File photo = new File(photodir+"/now.jpg");//不確定這樣寫O不OK
        String boundary = "*****";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        int rc;
        if(photo.exists()){//照片存在，那就開始上傳吧
            try{

                //上面是第二種策略
                //////////////////////////////////////////////////////////////////////


                FileInputStream fins = new FileInputStream(photo);
                HttpURLConnection conn = null;
                URL url = new URL(webip);
                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();

                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy//這一行開或關都不影響
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("file","now.jpg");//"now.jpg"那格要填filename 我不確定這樣寫對不對//應該沒錯

                int bytesRead, bytesAvailable, bufferSize;




                byte[] buffer;
                //rc=conn.getResponseCode();//另外弄一個conn 對 display.php 問rc這樣的檢查步驟會比較好
                //等等，這在App一開始的時候就會檢查Respons Code了，所以這邊可以不用管他

                //server OK 可以開始上傳 //拔掉if ，因為不需要檢查 rc=200
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                String wb="Content-Disposition: form-data; name=\"file[]\"; filename=\"now.jpg\" "+lineEnd+lineEnd;
                dos.writeBytes(wb);



                bytesAvailable = fins.available();


                bufferSize = Math.min(bytesAvailable,1048576);
                buffer = new byte[bufferSize];
                bytesRead = fins.read(buffer, 0, bufferSize);


                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fins.available();
                    bufferSize = Math.min(bytesAvailable, 1048576);
                    bytesRead = fins.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);




                rc = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                System.out.println(serverResponseMessage); //成功上傳，MySQL有記錄到，但網頁顯示的照片卻破圖了QQ
                //如果server回傳值是"OK"，表示這次有上傳成功
                //我用putty上去老師機器用ls-la看資料夾，發現沒有任何可疑的檔案被傳上去

                //感謝這個下面的留言說明解救了我，問題終於解決了 YA
                //http://www.myandroid.tw/bbs-topic-879.sea

                fins.close();
                dos.flush();
                dos.close();






            }catch (IOException ex) {
                //Logger.getLogger(this.getName()).log(Level.SEVERE, null, ex);
            }




        }
        //AsyncHttpClient client = new AsyncHttpClient();



        return 0;
    }

    public String getSdcardPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }

        return sdDir.toString() + "/";
    }



}
