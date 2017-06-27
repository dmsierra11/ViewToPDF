package com.example.danielsierra.selfnotepdf;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class SelfNoteFragment extends Fragment {


    private static final String LOG_TAG = "SelfnoteFragment";
    private static final int PERMISSION_EXTERNAL_STORAGE = 0;
    private EditText mSubjectEditText;
    private EditText mBodyEditText;
    private Button mSaveButton;
    private File myFile;

    public SelfNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        checkPermissions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mRootView = inflater.inflate(R.layout.fragment_self_note, container, false);
        mSubjectEditText = (EditText) mRootView.findViewById(R.id.edit_text_subject);
        mBodyEditText = (EditText) mRootView.findViewById(R.id.edit_text_body);
        mSaveButton = (Button) mRootView.findViewById(R.id.button_save);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSubjectEditText.getText().toString().isEmpty()){
                    mSubjectEditText.setError("Subject is empty");
                    mSubjectEditText.requestFocus();
                    return;
                }

                if (mBodyEditText.getText().toString().isEmpty()){
                    mBodyEditText.setError("Body is empty");
                    mBodyEditText.requestFocus();
                    return;
                }

                try {
                    createPDF();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return mRootView;
    }

    public void createPDF() throws IOException, DocumentException {

        File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "pdfdemo");
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
            Log.i(LOG_TAG, "Pdf Directory created");
        }


        //Create time stamp
        Date date = new Date() ;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);

        myFile = new File(pdfFolder + timeStamp + ".pdf");

        OutputStream output = new FileOutputStream(myFile);

        //Step 1
        Document document = new Document();

        //Step 2
        PdfWriter.getInstance(document, output);

        //Step 3
        document.open();

        //Step 4 Add content
        document.add(new Paragraph(mSubjectEditText.getText().toString()));
        document.add(new Paragraph(mBodyEditText.getText().toString()));

        //Step 5 close document
        document.close();

        promptForNextAction();
    }

    public void viewPDF(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(myFile), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    public void emailNote(){
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_SUBJECT,mSubjectEditText.getText().toString());
        email.putExtra(Intent.EXTRA_TEXT, mBodyEditText.getText().toString());
        Uri uri = Uri.parse(myFile.getAbsolutePath());
        email.putExtra(Intent.EXTRA_STREAM, uri);
        email.setType("message/rfc822");
        startActivity(email);
    }

    private void promptForNextAction() {
        final String[] options = { getString(R.string.label_email), getString(R.string.label_preview),
                getString(R.string.label_cancel) };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Note Saved, What Next?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equals(getString(R.string.label_email))){
                    emailNote();
                }else if (options[which].equals(getString(R.string.label_preview))){
                    viewPDF();
                }else if (options[which].equals(getString(R.string.label_cancel))){
                    dialog.dismiss();
                }
            }
        });

        builder.show();

    }

    public void checkPermissions(){
        Log.d(LOG_TAG, "Version code: "+ Build.VERSION.SDK_INT);
        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_EXTERNAL_STORAGE);
    }

    public boolean checkPermission(String permission){
        int result = ContextCompat.checkSelfPermission(getActivity(), permission);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public void requestPermission(String permission, int request_code){

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)){

//            Toast.makeText(this,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, request_code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this,"Permission Granted, Now you can access storage.",
//                            Toast.LENGTH_LONG).show();

                } else {
//                    Toast.makeText(this,"Permission Denied, You cannot access storage.",
//                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
