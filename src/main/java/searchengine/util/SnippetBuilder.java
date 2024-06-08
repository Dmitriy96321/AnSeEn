package searchengine.util;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import searchengine.dto.search.SearchResult;
import searchengine.model.PageEntity;
import searchengine.parser.LemmaParser;
import searchengine.repositories.JpaPagesRepository;
import searchengine.repositories.JpaSitesRepository;

@Component
@RequiredArgsConstructor
public class SnippetBuilder {
private final LemmaParser lemmaParser;
private final JpaPagesRepository pagesRepository;
private final JpaSitesRepository sitesRepository;

    public String getSnippet(PageEntity pageEntity, String query) {
//        PageEntity page = pagesRepository.findByPagePath(result.getUri(),
//                sitesRepository.findBySiteUrl(result.getSite()).getId());
        String search = Jsoup.parse(pageEntity.getContent()).body().text();
        StringBuilder snippet = new StringBuilder();



        if (search.contains(query)) {
            int index = search.indexOf(query);
            if (index != -1) {
                int wordStart = Math.max(index - 140, 0);
                int wordEnd = Math.min(index + query.length() + 140, search.length());
                snippet.append("...").append(search, wordStart, index)
                        .append("<b>").append(query).append("</b>")
                        .append(search, index + query.length(), wordEnd)
                        .append("...");
            } else {
                snippet.append("...")
                        .append("<b>").append(query).append("</b>")
                        .append(search, index + query.length(), Math.min(query.length() + 120, search.length()))
                        .append("...");
            }
            return snippet.toString();
        }

        for (String lemma : lemmaParser.getLemmasFromQuery(query)) {
            String word = lemma.substring(0, (lemma.length() * 80) / 100);
            int index = search.indexOf(word);
            if (search.contains(word)) {
                snippet.append(getStringForWordFromQuery(search, index));
            }
        }
        return snippet.toString();
    }

    private String getStringForWordFromQuery(String text, int index) {
        StringBuilder sb = new StringBuilder();
        for (int i = index - 1; i >= 0; i--) {
            sb.append(text.charAt(i));
            if (Character.isUpperCase(text.charAt(i))) {
                sb.reverse();
                break;
            } else if (i == 0) {
                sb.reverse();
                break;
            }
        }
        String word = text.substring(index, text.indexOf(" ", index) == -1 ?
                text.length() :
                text.indexOf(" ", index));
        sb.append("<b>").append(word).append("</b>");
        int counter = 0;
        for (int i = index + word.length(); i <= text.length() - 1; i++) {
            counter++;
            sb.append(text.charAt(i));
            if (counter == 90 || i == text.length() - 1) {
                sb.append(".../");
                break;
            }
        }
        return sb.toString();
    }
}
