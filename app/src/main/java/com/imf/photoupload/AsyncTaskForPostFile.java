package com.imf.photoupload;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class AsyncTaskForPostFile extends AsyncTask<String, Integer, Void>{
    HttpURLConnection conn = null;
    DataOutputStream dos = null;
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024 * 1024;
    private Context mContext;

    public AsyncTaskForPostFile(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(String... params) {

        File sourceFile = new File(params[0]);
        String post_url = params[1];
        String input_name = params[2];
        if (!sourceFile.isFile()) {
            Log.e("Fail to Uploading files","Files no exist");
            return null;
        }
        else
        {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(post_url);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setChunkedStreamingMode(0);
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file",input_name);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + input_name + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                long total_length = 0;
                int p;
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    total_length += bufferSize;
                    p =((int)(total_length * 100/(float)sourceFile.length()));
                    publishProgress(p);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                int serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){


                    try {


                        //HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        //connection.setRequestMethod("GET");
                        ////////////////////////////////////////////////原get改post
                        //connection.setRequestMethod("POST");

                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        Log.e("responcode", response.toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }









                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            }catch (Exception e) {


                e.printStackTrace();


            }

            return null;

        }
    }

    @Override
    protected void onPostExecute(Void result) {
        Intent intent = new Intent("PostFileComplete");







        this.mContext.sendBroadcast(intent);
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
        //    更新ProgressDialog的進度條
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Toast.makeText(this.mContext, String.valueOf(values[0])+" %", Toast.LENGTH_SHORT).show();
        //上面這行我先註解掉
    }

}