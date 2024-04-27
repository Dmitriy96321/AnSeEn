package searchengine.parser;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import org.apache.lucene.morphology.LuceneMorphology;
import searchengine.model.PageEntity;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


@Component
@Slf4j
@RequiredArgsConstructor
public class LemmaParser {
    private final LuceneMorphology luceneMorph;


    public Map<String, Integer> getLemmasForPage(PageEntity pageEntity) {
        String text = Jsoup.parse(pageEntity.getContent()).text();
        Map<String, Integer> map = new HashMap<>();
        splitTextIntoWords(Jsoup.parse(text).text()).stream()
                .filter(words -> !words.isEmpty())
                .map(luceneMorph::getNormalForms).forEach(wordFrms -> {
                    if (isNotFunctionalPartSpeech(luceneMorph.getMorphInfo(wordFrms.get(0)).toString())) {
                        map.merge(wordFrms.get(0), 1, Integer::sum);
                    }
                });
        return map;
    }


    private   List<String> splitTextIntoWords(String text) {
        return Arrays.stream(
                text.replaceAll("[^А-я ]","")
                .replaceAll("\\s{2,}"," ")
                .toLowerCase().split(" ")
        ).toList();
    }

    private boolean isNotFunctionalPartSpeech(String text) {
        AtomicBoolean out = new AtomicBoolean(false);
        String[] parts = {
                 "|A С мр,ед,им"
                , "|a Г дст,прш,мр,ед"
                , "|j Н"
                , "|Y КР_ПРИЛ ср,ед,од,но"
                , "|Y П мр,ед,вн,но"
                , "|K С ср,ед,им"
                , "|G С жр,ед,им"
                , "|a ИНФИНИТИВ дст"
        };
        Arrays.stream(parts).forEach(word -> {
                    if (text.contains(word)) {
                        out.set(true);
                    }
                }
        );
        return out.get();
    }
}
