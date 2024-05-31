package searchengine.parser;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import searchengine.model.PageEntity;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;


@Component
@Slf4j
@RequiredArgsConstructor
public class LemmaParser {
    private final RussianLuceneMorphology russian;
    private final EnglishLuceneMorphology english;
    private final String[] PARTS = {"ПРЕДЛ", "СОЮЗ", "МЕЖД", "ВВОДН", "ЧАСТ", "МС", "CONJ", "PART"};


    public List<String> getLemmasFromQuery(String query) {
        return getLemmas(query).keySet().stream().toList();
    }

    public Map<String, Integer> getLemmasForPage(PageEntity pageEntity) {
        String text = Jsoup.parse(pageEntity.getContent()).text();
        return getLemmas(text);
    }

    private List<String> splitTextIntoWords(String text) {
        List<String> wordList = new ArrayList<>();
        try (TokenStream tokenStream = new StandardAnalyzer().tokenStream("field", new StringReader(text))) {
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
                CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
                String word = token.toString();
                if (word.matches("[а-яА-Яa-zA-Z]+")) {
                    wordList.add(word);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return wordList;
    }
    private Map<String, Integer> getLemmas(String text) {
        Map<String, Integer> map = new HashMap<>();
        splitTextIntoWords(Jsoup.parse(text).text()).stream()
                .filter(words -> !words.isEmpty())
                .map(word -> word.matches("^[A-z]*$") ? english.getNormalForms(word) :
                        russian.getNormalForms(word.replaceAll("[^а-яА-Я]", ""))
                ).forEach(wordForms -> map.merge(wordForms.get(0),
                        wordForms.get(0).matches("^[A-z]*$") ?
                                (Arrays.stream(PARTS).noneMatch(
                                        english.getMorphInfo(wordForms.get(0)).toString()::contains) ? 1 : 0) :
                                (Arrays.stream(PARTS).noneMatch(
                                        russian.getMorphInfo(wordForms.get(0)).toString()::contains) ? 1 : 0),
                        Integer::sum)
                );
        return map;
    }
}
