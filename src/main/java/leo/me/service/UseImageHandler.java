package leo.me.service;

import static leo.me.Constants.DRAWING_BUCKET_NAME;
import static leo.me.Constants.USER_BUCKET_NAME;
import static leo.me.utils.CharUtils.encodeBucketName;

import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;

/**
 * use choose one image, send the url of that image, this handler will download the image, save and return it's base64 data.
 */
public class UseImageHandler implements Handler {

    /**
     * {
     *     words: [''],
     *     wechatId: '',
     *     url: ''
     * }
     * @param request
     * @return
     */
    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        String word = request.getWords().get(0);
        String wechatId = request.getWechatId();
        String url = request.getImageUrl();

        String imageData = loadImage(url);
        String bucketPath = String.format("%s/%s/%s", DRAWING_BUCKET_NAME, encodeBucketName(word), wechatId);

        s3Client.putObject(USER_BUCKET_NAME, bucketPath, imageData);

        MoerdoResponse response = new MoerdoResponse();
        response.setImageContent(imageData);
        return response;
    }
}
