package leo.me.service;

import static leo.me.Constants.CSE_CX;
import static leo.me.Constants.CSE_DEVELOPER_KEY;

import com.google.api.services.customsearch.model.Result;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import leo.me.exception.ServerSideException;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetImageHandler implements Handler{

    @Override
    public MoerdoResponse handle(MoerdoRequest request) {
        String keywords = request.getWords().stream().collect(Collectors.joining(" "));

        if (Strings.isNullOrEmpty(CSE_DEVELOPER_KEY) || Strings.isNullOrEmpty(CSE_CX)) {
            throw new ServerSideException("Environment variables CSE_DEVELOPER_KEY, CSE_CX are missing.");
        }

        List<Result> items = new CseService(CSE_DEVELOPER_KEY, CSE_CX).searchImages(keywords);
        if (items == null || items.isEmpty()) {
            throw new ServerSideException("查询图片没有返回结果。");
        }

        List<Map<String, String>> links = items.stream()
                .map(item -> ImmutableMap.of("title", item.getTitle(), "link", item.getLink()))
                .collect(Collectors.toList());

        MoerdoResponse response = new MoerdoResponse();
        response.setLinks(links);

        return response;
    }
}
