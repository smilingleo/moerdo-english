package leo.me.service;

import static leo.me.Constants.DRAWING_BUCKET_NAME;
import static leo.me.Constants.USER_BUCKET_NAME;

import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;

/**
 * Allow user upload drawing image.
 *
 * The S3 structure:
 * `/moerdo-user/drawing/<word>/<wechat.id>`
 *
 * `word` is encoded:
 * 1. replace ` ` with `_`
 * 2. lower case
 *
 */
public class FileUploadHandler implements Handler {

    /**
     * Request body:
     * {
     *     "wechatId": "",
     *     "words": [""],
     *     "imageContent": ""
     * }
     * @param request
     * @return
     */
    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        String wechatId = request.getWechatId();
        String word = request.getWords().get(0);
        String imageData = request.getImageContent();

        String bucketPath = String.format("%s/%s/%s", DRAWING_BUCKET_NAME, encodeBucketName(word), wechatId);

        s3Client.putObject(USER_BUCKET_NAME, bucketPath, imageData);

        MoerdoResponse response = new MoerdoResponse();
        return response;
    }

    private String encodeBucketName(String word) {
        return word.replaceAll("\\s", "_").toLowerCase();
    }
}
