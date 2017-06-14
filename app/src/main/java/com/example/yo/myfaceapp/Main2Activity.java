package com.example.yo.myfaceapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Accessory;
import com.microsoft.projectoxford.face.contract.Emotion;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FacialHair;
import com.microsoft.projectoxford.face.contract.Hair;
import com.microsoft.projectoxford.face.contract.HeadPose;
import com.microsoft.projectoxford.face.contract.Makeup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;




public class Main2Activity extends AppCompatActivity {
    private class DetectionTask extends AsyncTask<InputStream, String, Face[]> {
        private boolean mSucceed = true;


        @Override
        protected Face[] doInBackground(InputStream... params) {
            FaceServiceClient faceServiceClient = getFaceServiceClient();
            try {
                publishProgress("얼굴 인식중...");

                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        true,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        new FaceServiceClient.FaceAttributeType[]{
                                FaceServiceClient.FaceAttributeType.Age,
                                FaceServiceClient.FaceAttributeType.Gender,
                                FaceServiceClient.FaceAttributeType.Smile,
                                FaceServiceClient.FaceAttributeType.Glasses,
                                FaceServiceClient.FaceAttributeType.FacialHair,
                                FaceServiceClient.FaceAttributeType.Emotion,
                                FaceServiceClient.FaceAttributeType.HeadPose,
                                FaceServiceClient.FaceAttributeType.Accessories,
                                FaceServiceClient.FaceAttributeType.Blur,
                                FaceServiceClient.FaceAttributeType.Exposure,
                                FaceServiceClient.FaceAttributeType.Hair,
                                FaceServiceClient.FaceAttributeType.Makeup,
                                FaceServiceClient.FaceAttributeType.Noise,
                                FaceServiceClient.FaceAttributeType.Occlusion
                        });
            } catch (Exception e) {
                mSucceed = false;
                publishProgress(e.getMessage());

                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setMessage(progress[0]);
            setInfo(progress[0]);
        }

        @Override
        protected void onPostExecute(Face[] result) {

            setUiAfterDetection(result, mSucceed);
        }
    }

    private static final int REQUEST_SELECT_IMAGE = 0;

    private Uri mImageUri;


    private Bitmap mBitmap;

    ProgressDialog mProgressDialog;

    public FaceServiceClient getFaceServiceClient() {
        return sFaceServiceClient;
    }

    private FaceServiceClient sFaceServiceClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("잠시만 기다려 주세요");

        setDetectButtonEnabledStatus(false);
        sFaceServiceClient = new FaceServiceRestClient("https://westcentralus.api.cognitive.microsoft.com/face/v1.0", "3129038c59b24525b9a6d9bad799d038");
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ImageUri", mImageUri);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mImageUri = savedInstanceState.getParcelable("ImageUri");
        if (mImageUri != null) {
            mBitmap = Image.loadSizeLimitedBitmapFromUri(
                    mImageUri, getContentResolver());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    mImageUri = data.getData();
                    mBitmap = Image.loadSizeLimitedBitmapFromUri(
                            mImageUri, getContentResolver());
                    if (mBitmap != null) {
                        ImageView imageView = (ImageView) findViewById(R.id.image);
                        imageView.setImageBitmap(mBitmap);

                    }

                    FaceListAdapter faceListAdapter = new FaceListAdapter(null);
                    ListView listView = (ListView) findViewById(R.id.list_detected_faces);
                    listView.setAdapter(faceListAdapter);

                    setInfo("");

                    setDetectButtonEnabledStatus(true);
                }
                break;
            default:
                break;
        }
    }

    public void selectImage(View view) {
        Intent intent = new Intent(this, Main3Activity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    public void detect(View view) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        new DetectionTask().execute(inputStream);

        setAllButtonsEnabledStatus(false);
    }

    private void setUiAfterDetection(Face[] result, boolean succeed) {
        mProgressDialog.dismiss();

        setAllButtonsEnabledStatus(true);

        setDetectButtonEnabledStatus(false);

        if (succeed) {
            String detectionResult;
            if (result != null) {
                detectionResult = result.length + "개의 얼굴이"
                        + (result.length != 1 ? "" : "") + " 감지되었습니다.";

                ImageView imageView = (ImageView) findViewById(R.id.image);
                imageView.setImageBitmap(Image.drawFaceRectanglesOnBitmap(
                        mBitmap, result, true));

                FaceListAdapter faceListAdapter = new FaceListAdapter(result);

                ListView listView = (ListView) findViewById(R.id.list_detected_faces);
                listView.setAdapter(faceListAdapter);
            } else {
                detectionResult = "얼굴이 감지되지 않았습니다.";
            }
            setInfo(detectionResult);
        }

        mImageUri = null;
        mBitmap = null;
    }

    private void setDetectButtonEnabledStatus(boolean isEnabled) {
        Button detectButton = (Button) findViewById(R.id.detect);
        detectButton.setEnabled(isEnabled);
    }

    private void setAllButtonsEnabledStatus(boolean isEnabled) {
        Button selectImageButton = (Button) findViewById(R.id.select_image);
        selectImageButton.setEnabled(isEnabled);

        Button detectButton = (Button) findViewById(R.id.detect);
        detectButton.setEnabled(isEnabled);
    }

    private void setInfo(String info) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);
    }

    private class FaceListAdapter extends BaseAdapter {
        List<Face> faces;

        List<Bitmap> faceThumbnails;

        FaceListAdapter(Face[] detectionResult) {
            faces = new ArrayList<>();
            faceThumbnails = new ArrayList<>();

            if (detectionResult != null) {
                faces = Arrays.asList(detectionResult);
                for (Face face : faces) {
                    try {
                        faceThumbnails.add(Image.generateFaceThumbnail(mBitmap, face.faceRectangle));
                    } catch (IOException e) {
                        setInfo(e.getMessage());
                    }
                }
            }
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getCount() {
            return faces.size();
        }

        @Override
        public Object getItem(int position) {
            return faces.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.customlistview, parent, false);
            }
            convertView.setId(position);

            ((ImageView) convertView.findViewById(R.id.face)).setImageBitmap(faceThumbnails.get(position));

            if(faces.get(position).faceAttributes.gender.equals("male")){
                faces.get(position).faceAttributes.gender = "남성";
            }else if(faces.get(position).faceAttributes.gender.equals("female")){
                faces.get(position).faceAttributes.gender = "여성";
            }
            DecimalFormat formatter = new DecimalFormat("#0.0");
            String face_description = String.format("나이: %s  성별: %s\n머리카락 색깔: %s  수염: %s\n화장: %s  %s\n악세사리: %s",
                    faces.get(position).faceAttributes.age,
                    faces.get(position).faceAttributes.gender,
                    getHair(faces.get(position).faceAttributes.hair),
                    getFacialHair(faces.get(position).faceAttributes.facialHair),
                    getMakeup((faces.get(position)).faceAttributes.makeup),
                    getEmotion(faces.get(position).faceAttributes.emotion),
                    getAccessories(faces.get(position).faceAttributes.accessories)
            );
            ((TextView) convertView.findViewById(R.id.text_detected_face)).setText(face_description);

            return convertView;
        }

        private String getHair(Hair hair) {
            if (hair.hairColor.length == 0) {
                if (hair.invisible)
                    return "머리카락이 보이지 않습니다";
                else
                    return "대머리";
            } else {
                int maxConfidenceIndex = 0;
                double maxConfidence = 0.0;

                for (int i = 0; i < hair.hairColor.length; ++i) {
                    if (hair.hairColor[i].confidence > maxConfidence) {
                        maxConfidence = hair.hairColor[i].confidence;
                        maxConfidenceIndex = i;
                    }
                }

                return hair.hairColor[maxConfidenceIndex].color.toString();
            }
        }

        private String getMakeup(Makeup makeup) {
            return (makeup.eyeMakeup || makeup.lipMakeup) ? "했다" : "안했다";
        }

        private String getAccessories(Accessory[] accessories) {
            if (accessories.length == 0) {
                return "악세사리가 없습니다";
            } else {
                String[] accessoriesList = new String[accessories.length];
                for (int i = 0; i < accessories.length; ++i) {
                    accessoriesList[i] = accessories[i].type.toString();
                }

                return TextUtils.join(",", accessoriesList);
            }
        }

        private String getFacialHair(FacialHair facialHair) {
            return (facialHair.moustache + facialHair.beard + facialHair.sideburns > 0) ? "있다" : "없다";
        }

        private String getEmotion(Emotion emotion) {
            String emotionType = "";
            double emotionValue = 0.0;
            if (emotion.anger > emotionValue) {
                emotionValue = emotion.anger;
                emotionType = "화남";
            }
            if (emotion.contempt > emotionValue) {
                emotionValue = emotion.contempt;
                emotionType = "경멸";
            }
            if (emotion.disgust > emotionValue) {
                emotionValue = emotion.disgust;
                emotionType = "싫어함";
            }
            if (emotion.fear > emotionValue) {
                emotionValue = emotion.fear;
                emotionType = "공포";
            }
            if (emotion.happiness > emotionValue) {
                emotionValue = emotion.happiness;
                emotionType = "기쁨";
            }
            if (emotion.neutral > emotionValue) {
                emotionValue = emotion.neutral;
                emotionType = "자연스러움";
            }
            if (emotion.sadness > emotionValue) {
                emotionValue = emotion.sadness;
                emotionType = "슬픔";
            }
            if (emotion.surprise > emotionValue) {
                emotionValue = emotion.surprise;
                emotionType = "놀람";
            }
            return String.format("%s: %f", emotionType, emotionValue);
        }
    }
}
