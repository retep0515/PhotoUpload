package com.imf.photoupload;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.AsyncTask;
import android.content.Intent;
import android.util.Base64;
import android.os.Environment;
import android.widget.Toast;

//import org.apache.http.*;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.*;


public class MainActivity extends AppCompatActivity {
    ImageView iv;
    static final int CAM_REQUEST = 1;
    //String webip="http://140.113.72.101/photo/";    //芯莆筆電server
    //String webip="http://140.113.26.45/photo/";    //Mb09server
    String webip="http://192.168.0.100/photo/";  //小白盒wifi

    //String webip="http://140.113.2.218/~p0213453/AndroidDB/";  //學校計中的server


    //




    String photodir=getSdcardPath()+"/demo";
    TextView sever_state;
    int lastserverRespondcode=-1;
    TextView panswer,pscore;
    int lid=1;  //last id
    String lastid="last";
    String answer="";
    String score=""; //先宣告成字串形式比較好測試，之後考慮改成int
    boolean photoready=false;

    private static final String TAG = "MainActivity"; //for debug message



    /*將來準備要增加的功能

    1.偵測server狀態
    2.講中文
    3.取得使用者的GPS座標
    4.偵測環境亮度
    5.連拍或錄影

    */
    //語音辨識  ///////////////////////////////////////////////////////////////////////////////////////
    //##如果要拿去比賽的功能是以幼教學習軟體為主，那就要同時可以唸中英文，因此我還要把他弄成可以講中文(匯入中文語音資料包，做一些設定....)
    protected TextToSpeech tts; //private 改 protected 試試看
    private TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() {
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
// 如果該語言資料不見了或沒有支援則無法使用
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "This Language is not supported");
                } else {
// 語調(1為正常語調；0.5比正常語調低一倍；2比正常語調高一倍)
                    tts.setPitch((float) 1.5);
// 速度(1為正常速度；0.5比正常速度慢一倍；2比正常速度快一倍)
                    tts.setSpeechRate((float) 1.0);
// 設定要說的內容文字
                    tts.speak(answer, TextToSpeech.QUEUE_FLUSH, null);
                }
            } else {
                Toast.makeText(MainActivity.this, "Initialization Failed!",
                        Toast.LENGTH_LONG).show();
            }
        }
    };

///////////////////////////////////////////////////////////////////////////////////////////////////






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //find view by id 一定要寫在這行之下



        //下面這兩行code用來改變action bar 的顏色/////////////////////////////////////////////////////////////////////
        //ActionBar bar = getActionBar();
        //bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0000ff")));

        sever_state = (TextView)findViewById(R.id.ss);
        sever_state.setVisibility(TextView.INVISIBLE);//需要顯示的時候再把這行註解掉

        panswer = (TextView)findViewById(R.id.photoAnswer);
        pscore = (TextView)findViewById(R.id.photoScore);

        iv= (ImageView)findViewById(R.id.imageView2);


        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });





        ////////////////////////////////////////////////////////////////////////////
        //改寫成thread的形式，才不會TLE




















        ////////////////////////////////////////////////////////////////////////////////







/*
        try {
            //TextView sever_state = (TextView)findViewById(R.id.ss);
            int rc;
            HttpURLConnection conn = null;
            URL url = new URL(webip);
            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            rc = conn.getResponseCode(); //為何無法偵測 QQ
            if(rc==200){sever_state.setText("server status : ON");}
            else{sever_state.setText("server status : OFF");}
        }catch (Exception e){
            //do nothing
        }
*/




        Button gotobutton = (Button) findViewById(R.id.gotocamara);
        gotobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //global variable initialization
                photoready=true;
                lastid="last";
                answer="";
                score="";
                Intent imgcap = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = getFile();
                imgcap.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                startActivityForResult(imgcap, CAM_REQUEST);
                //startActivity(imgcap);
                //拍完照片之後確實有開個demo資料夾儲存照片，但是接下來我的程式就當機沒有回應了
                //SQL沒更新成功
                //goup();//Debugggggggggggggggggg
                //想到一個可以試試看的寫法，用一個boolean flag 去紀錄現在是不是跳進camara再回來
                //在進系統 camara intent 之前先改flag
                //回來後 main_activity 從 onStop() => onRestart() 檢查flag 來判斷是否要進確認上傳的activity
                //但這前提是我至少要把兩個activity 的intent call 寫成功


                //#偵測server狀態要寫成thread的形式，才不會因為網頁的回應速度太慢還來不及取得結果就被kill掉了
                //sever_state.setText("You click the button");

                //丟intent 進下一個 activity
                //Intent intent=new Intent(this, SecondActivity.class);startActivity(intent);
            }
        });





        //////////////////////////////////////////////////////////
        //需要偵測server狀態的時候再把下面的註解開啟
        //Thread t0 = new Thread(checkserver);
        //t0.start();
        /////////////////////////////////////////////////////////




        //偵測完sever狀態之後 填入 ON / OFF
        //sever_state.setText("You click the button");

    }


















    ///////////////////////////////////////////////////////////////////////////////////////












    public String getHTML(String strURL) {
        String buf="";String s="";
        int now;

        URLConnection conn = null;
        InputStreamReader inStream;

        try {
            URL url_address = new URL(strURL);

            //Log.e("run to here.", "before setRequestProperty");
            conn=url_address.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.19 (KHTML, like Gecko) Chrome/0.3.154.9 Safari/525.19");
            conn.setRequestProperty("Accept-Charset", "utf-8");//測試看看加上這一行後資料夾名稱為中文亂碼的問題有沒有解決///////////////////////////////////////////////////




            //下一行有bug
            inStream = new InputStreamReader(conn.getInputStream(),"UTF-8"); //如果是無效的URL，會在這一行丟出FileNotFoundException
            //inStream = new InputStreamReader(conn.getInputStream()); //如果是無效的URL，會在這一行丟出FileNotFoundException

            BufferedReader br = new BufferedReader(inStream);
            while((buf=br.readLine())!=null){
                s=s+buf;
            }




        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Log.e("run to here.","before return");




        return s;
    }



/*
    @Override
    protected void onStart() {
        super.onStart();
        try {
            TextView sever_state = (TextView)findViewById(R.id.ss);
            int rc;
            HttpURLConnection conn = null;
            URL url = new URL(webip);
            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            rc = conn.getResponseCode(); //為何無法偵測 QQ
            if(rc==200){sever_state.setText("server status : ON");}
            else{sever_state.setText("server status : OFF");}
        }catch (Exception e){
            //do nothing
        }




    }

*/






    private final BroadcastReceiver AsyncTaskForPostFileReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(MainActivity.this, "PostFileComplete", Toast.LENGTH_SHORT).show();
            //開thread 那一整段移到這裡
            // get ? 名稱="........"
            // ###################__________###############_____________
            // TextView set text

        }
    };





    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        String path = photodir + "/now.jpg";

        //iv.setImageDrawable(Drawable.createFromPath(path));
        double hi, wi, scale;
        int iw, ih;
        BitmapFactory.Options options;
        options = new BitmapFactory.Options();
        options.inSampleSize = 2;





        if(photoready==true){//避免重複進來，重複上傳
            photoready=false;


        ////////////////////////////////////////////////////////////////////////////////
        Thread t1 = new Thread(getnowid);  //跟server要id編號
        t1.start();

            try { //避免server太忙，id過很久才回傳，所以先sleep 1秒
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            Bitmap bmp = BitmapFactory.decodeFile(path, options);
        hi = bmp.getHeight();
        wi = bmp.getWidth();
        scale = Math.sqrt(1000000 / (hi * wi));
        iw = (int) (wi * scale);
        ih = (int) (hi * scale);
        //Log.e(hi+" "+wi+" "+ih+" "+iw+" "+scale, "debug");


        //Bitmap b = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length)
        Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, iw, ih, false);
        iv.setImageBitmap(bmp2);

        //iv.setImageBitmap(bmp);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(photodir + "/now2.jpg"));
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            //Log.e("goin catch",e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //DB connection////////////////////////////////////////////////////////////////////////

        /*
        String userName="p0213453";
        String passWord="EVC9P0";

        try {
            Log.e("ready to connect DB","debug");
            Class.forName("com.mysql.jdbc.Driver");
            //Connection con = DriverManager.getConnection("jdbc:jtds:sqlserver://140.113.2.218",userName,passWord);
            //Connection con = DriverManager.getConnection("jdbc:mysql://140.113.2.218:3306/db0213453",userName,passWord);
            Connection con = DriverManager.getConnection("jdbc:mysql://140.113.2.218",userName,passWord);
            //Connection con = DriverManager.getConnection("jdbc:mysql://140.113.2.218:3306/db0213453",userName,passWord);
            Log.e("connect OK","debug");
            //MysqlDataSource ds;
            //ds = new com.mysql.jdbc.jdbc2.optional.MysqlDataSource();
            ResultSet rs;
            Statement stat = null;
            String qs="select * from imf"; //query
            //String rs;
            stat = con.createStatement();
            rs = stat.executeQuery(qs);
            ResultSetMetaData rsmd = rs.getMetaData();

            //PreparedStatement pst = null;
            //pst=con.prepareStatement(qs);
            //rs=pst.executeQuery();
            Log.e(""+rs.getInt(1),"debug");

            while(rs.next())
            {
                lid=rs.getInt(1);
                lastid=""+lid;
                Log.e(rsmd.getColumnName(1),lastid);
            }
            //Log.e("id",rs);

        } catch (SQLException e) {
            Log.e("connect fail 1",e.toString());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e("connect fail 2",e.toString());
            e.printStackTrace();
        }
        */

        registerReceiver(AsyncTaskForPostFileReceiver, new IntentFilter("PostFileComplete"));
        AsyncTaskForPostFile PostFile = new AsyncTaskForPostFile(MainActivity.this);
        PostFile.execute(photodir + "/now2.jpg", webip + "test.php", lastid + ".jpg");
        //加上這行debug
            //訪問 ok.php的部分我寫在  Async...java 裡面

        //unregisterReceiver(AsyncTaskForPostFileReceiver);
        //這行好像可有可無不會影響耶，之後考慮拿掉


        //接下來等待辨識結果
        answer = "";
        Thread t2 = new Thread(getanswer);
        t2.start();


        while (answer.length() <= 2) {

            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("sleep()失敗", "debug");
            }

        }
        panswer.setText("辨識結果: " + answer);
            pscore.setText("照片分數: "+score);

        //顯示辨識結果的code我寫在thread裡面


        //接下來要把結果唸出來

        String toSpeak = answer;


        tts = new TextToSpeech(MainActivity.this, ttsInitListener);

        Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
        tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);


    }///////////////////////////////////////////////////////////////////////////////////////////////////











        //得到結果以丟toast 的形式呈現好了










    }




    public String getSdcardPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }




        return sdDir.getPath();  //AVD logcat 指出 ，這行使用的to string 會有問題，要改
        //Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.io.File.toString()' on a null object reference at com.imf.photoupload.MainActivity.getSdcardPath(MainActivity.java:151)
    }








    private File getFile(){
    File folder= new File(photodir);

        if(!folder.exists()){folder.mkdir();}
        File img=new File(folder,"now.jpg");
        return img;

    }


    Runnable checkserver = new Runnable() {
        @Override
        public void run() {
            try {

                HttpURLConnection conn = null;

                URL url_address = new URL(webip+"ok.php"); //單純用id=0來查詢，確認server目前是否有開機
                //lastserverRespondcode=conn.getResponseCode();

                // Open a HTTP  connection to  the URL

                conn = (HttpURLConnection) url_address.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.19 (KHTML, like Gecko) Chrome/0.3.154.9 Safari/525.19");
                conn.setRequestProperty("Accept-Charset","utf-8");


                InputStreamReader inStream;
                String s,buf;
                s="";

                    //inStream = new InputStreamReader(conn.getInputStream(),"UTF-8"); //如果是無效的URL，會在這一行丟出FileNotFoundException
                    //BufferedReader br = new BufferedReader(inStream);
                    //while((buf=br.readLine())!=null) {s = s + buf;}


                lastserverRespondcode =conn.getResponseCode(); //為何無法偵測 QQ
                if(lastserverRespondcode==200){sever_state.setText("server status : ON");}
                else{sever_state.setText("server status : OFF");}


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    };



















    Runnable getanswer = new Runnable() {
        public void run() {





            int i, k;
            String now;// = getHTML(webip+"getResult.php?id=" + lastid);
            now = getHTML(webip + "getResult.php?id=" + lastid);
            i = now.indexOf("body");
            k = now.lastIndexOf("body");
            i += 5;
            k -= 2;
            now = now.substring(i, k);

            k=now.indexOf("##########");

            answer=now.substring(0,k);

            score=now.substring(k+10,now.length());

            Log.e("substring", now+"_"+answer+"_"+score);

            while(answer.length() <= 2){
                 try {
                     sleep(1000); //休息1秒鐘之後再問一次
                 } catch (InterruptedException e) {
                     Log.e("sleep()失敗","debug");
                     e.printStackTrace();

                 }
                 now = getHTML(webip+"getResult.php?id=" + lastid);
                i = now.indexOf("body");
                k = now.lastIndexOf("body");
                i += 5;
                k -= 2;
                now = now.substring(i, k);

                k=now.indexOf("##########");

                answer=now.substring(0,k);

                score=now.substring(k+10,now.length());

                Log.e("substring", now+"_"+answer+"_"+score);

            }

            //panswer.setText(answer);
            //只有主activity 有權限改TextView,所以不能寫在這裡




        }

    };
    Runnable getnowid = new Runnable() {
        public void run() {

            String now=getHTML(webip+"nowid.php");
            int i,k;

            i=now.indexOf("body");
            k=now.lastIndexOf("body");
            k-=2;
            now=now.substring(i,k);



            //debug

            i=now.indexOf("id");
            i+=3;
            now=now.substring(i,now.length());

            Log.e("substring",now);
            lastid=now;



        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
// Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
























    }


/*


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







public void goup(){
        Intent intent=new Intent(this,upload.class);
        startActivity(intent);
    }





 */
