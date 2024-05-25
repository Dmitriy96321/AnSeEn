package searchengine.parser;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import org.apache.lucene.morphology.LuceneMorphology;
import searchengine.model.PageEntity;

import java.util.*;


@Component
@Slf4j
@RequiredArgsConstructor
public class LemmaParser {
    private final LuceneMorphology luceneMorph;
    private final String[] PARTS = {
            "|A С мр,ед,им"
            , "|a Г дст,прш,мр,ед"
            , "|j Н"
            , "|Y КР_ПРИЛ ср,ед,од,но"
            , "|Y П мр,ед,вн,но"
            , "|K С ср,ед,им"
            , "|G С жр,ед,им"
            , "|a ИНФИНИТИВ дст"
    };


    public List<String> getLemmasFromQuery(String query) {
        Set<String> lemmas = new HashSet<>();
        splitTextIntoWords(query).stream()
                .filter(words -> !words.isEmpty())
                .map(luceneMorph::getNormalForms).forEach(wordForms -> {
                    if (Arrays.stream(PARTS).anyMatch(luceneMorph.getMorphInfo(wordForms.get(0)).toString()::contains)){
                        lemmas.add(wordForms.get(0));
                    }
                });
        return lemmas.stream().toList();
    }


    public Map<String, Integer> getLemmasForPage(PageEntity pageEntity) {
        String text = Jsoup.parse(pageEntity.getContent()).text();
        Map<String, Integer> map = new HashMap<>();
        splitTextIntoWords(Jsoup.parse(text).text()).stream()
                .filter(words -> !words.isEmpty())
                .map(luceneMorph::getNormalForms).forEach(wordForms -> {
                    if (Arrays.stream(PARTS).anyMatch(luceneMorph.getMorphInfo(wordForms.get(0)).toString()::contains)){
                        map.merge(wordForms.get(0), 1, Integer::sum);
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
}
