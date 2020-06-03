package leo.me.service;

import static leo.me.Constants.CSE_CX;
import static leo.me.Constants.CSE_DEVELOPER_KEY;

import com.google.api.services.customsearch.model.Result;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import leo.me.exception.ServerSideException;
import leo.me.lambda.MoerdoRequest;
import leo.me.lambda.MoerdoResponse;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        // filter out the duplicated images, and only return 9 images to show in a 3*3 layout.
        final List<Map<String, String>> list = new LinkedList<>();
        links.forEach(link -> {
            // if the title is not in the list yet
            if (!list.stream().filter(item -> Objects.equals(item.get("title"), link.get("title"))).findFirst().isPresent()) {
                list.add(link);
            }
        });

        MoerdoResponse response = new MoerdoResponse();
        if (list.size() > 9) {
            response.setLinks(list.subList(0, 9));
        } else {
            response.setLinks(list);
        }


        return response;
    }
}
