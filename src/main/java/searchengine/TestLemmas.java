package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestLemmas {
    public static void main(String[] args) {
        String text =
//                "Правовые документы Продукты КЭДО для сотрудников КЭДО с нерезидентами РФ ЭДО с контрагентами ЭДО для банков ЭДО с самозанятыми Тарифы Тарифы ЭДО Тарифы КЭДО Акции КЭДО с взаимозачётом Кейсы WorldClass АльфаСтрахование Совкомбанк Жизнь Manpower Увелка Кнопка Artsofte Все кейсы Партнёры Партнёрская программа Решения с партнёрами О Nopaper СМИ о нас Блог FAQ База знаний Войти Регистрация Digital Workplace (Nopaper&КОРУС Консалтинг) Обработка персональных данных на сайте Согласие на обработку персональных данных Политика организации в отношении обработки персональных данных на сайте nopaper.ru Лицензионный договор на приобретение программы Лицензионный Договор-оферта Приложение №1 - Правила использования отдельных компонентов Приложение №2 - Поручение Лицензиата на обработку персональных данных Пользовательское соглашение Пользовательское (лицензионное) соглашение о предоставлении прав пользования сервисом «nopaper» Приложение №1 - Правила использования Сервиса Приложение №2 - Соглашение об использовании электронной подписи Приложение №3 - Политика конфиденциальности обработки персональных данных Сервиса Приложение №4 - Положение о неразглашении информации Приложение №5 - Тарифы Приложение №6 - Регламент процедуры разрешения конфликтных ситуаций Приложение №7 - Форма Акта признания ключа проверки ЭП Обработка персональных данных в сервисе Приложение №3 - Политика конфиденциальности обработки персональных данных Сервиса Согласие субъекта на обработку персональных данных Соглашение для партнёров Оферта на заключение партнерского соглашения Приложение №1 к Оферте - Партнерская программа Правовые документы г. Москва, ИЦ Сколково, Большой бульвар, д. 42, стр. 1, эт. 0, пом. 264, рм 4. Зарегистрированы в реестрах: Компания-резидент: 2024 ООО «Акоммерс» Интеллектуальная собственность Пользовательское соглашение Политика организации в отношении обработки персональных данных на сайте nopaper.ru Согласие на обработку персональных данных Правовые документы SLA технической поддержки 8 (800) 550-65-30 hello@nopaper.ru Техническая поддержка Информация о поддерживаемых Nopaper браузеров и ОС"


                "Повторное появление леопарда в Осетии позволяет предположить," +
                " что леопард постоянно обитает в некоторых районах Северного Кавказа."
                ;

        try {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            Map<String,Integer> map = new HashMap<>();
            splitTextIntoWords(text).stream().map(luceneMorph::getNormalForms).forEach(wordFrms -> {
                if (isNotFunctionalPartSpeech(luceneMorph.getMorphInfo(wordFrms.get(0)).toString())) {
                    map.merge(wordFrms.get(0), 1, Integer::sum);
                }
            });
            map.entrySet().forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        System.out.println(getText().replaceAll("\\<.*?\\>", ""));
    }
    private static List<String> splitTextIntoWords(String text) {
         String w = text.replaceAll("[^ А-я]","")
                .replaceAll("\\s{2,}"," ")
                .toLowerCase()
                 ;
        System.out.println(w)
//                .split(" ")
                ;
        String[] out = w.split(" ");

        System.out.println(Arrays.stream(out).toList());

        return Arrays.stream(out).toList();
    }


    private static boolean isNotFunctionalPartSpeech(String text) {
        AtomicBoolean out = new AtomicBoolean(false);
        String[] parts = {
                "|A С мр,ед,им"
                , "|a Г дст,прш,мр,ед"
                , "|Y КР_ПРИЛ ср,ед,од,но"
                , "|Y П мр,ед,вн,но"
                , "|a ИНФИНИТИВ дст"
                , "|A С мр,ед,им"
                , "|K С ср,ед,им"

        };
        Arrays.stream(parts).forEach(o -> {
                    if (text.contains(o)) {
                        out.set(true);
                    }
                }
        );
        return out.get();
    }
}
