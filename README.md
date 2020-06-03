# 世凝听记（后端）

This is the server side of the WeChat mini program.

## Technologies Used

* `AWS Polly` for text to speech
* `AWS Lambda` hosting the code
* `AWS S3` stores the data
* `AWS API Gateway` to expose the features.
* `sqlite` file database.  

## Prerequisites

* Wechat Miniprogram token, set `APP_ID`, `APP_SECRET` environment variables.
* Google CSE Token, `G_API_CX`, `G_API_DEVELOPER_KEY` environment variables. (also need to make sure image search is enabled.)

