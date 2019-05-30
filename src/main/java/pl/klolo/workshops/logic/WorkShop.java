package pl.klolo.workshops.logic;

import pl.klolo.workshops.domain.Currency;
import pl.klolo.workshops.domain.*;
import pl.klolo.workshops.mock.HoldingMockGenerator;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class WorkShop {
    /**
     * Lista holdingów wczytana z mocka.
     */
    private final List<Holding> holdings;

    private final Predicate<User> isWoman = user -> user.getSex().equals(Sex.WOMAN);


    WorkShop() {
        final HoldingMockGenerator holdingMockGenerator = new HoldingMockGenerator();
        holdings = holdingMockGenerator.generate();
    }

    /**
     * Metoda zwraca liczbę holdingów w których jest przynajmniej jedna firma.
     */
    long getHoldingsWhereAreCompanies() {
        return holdings.stream().map(holding -> holding.getCompanies())
                .filter(companies -> companies.size() != 0).count();
    }

    /**
     * Zwraca nazwy wszystkich holdingów pisane z małej litery w formie listy.
     */
    List<String> getHoldingNames() {
        return holdings.stream().map(holding -> holding.getName().toLowerCase())
                .collect(Collectors.toList());


    }

    /**
     * Zwraca nazwy wszystkich holdingów sklejone w jeden string i posortowane.
     * String ma postać: (Coca-Cola, Nestle, Pepsico)
     */
    String getHoldingNamesAsString() {
        return "(" + holdings.stream().map(holding -> holding.getName()).sorted().collect(Collectors.joining(", ")) + ")";
    }

    /**
     * Zwraca liczbę firm we wszystkich holdingach.
     */
    long getCompaniesAmount() {
//        holdings.stream().mapToLong(x->x.getCompanies().size()).sum();
        return holdings.stream().collect(Collectors.summingLong(value -> value.getCompanies().size()));
    }

    /**
     * Zwraca liczbę wszystkich pracowników we wszystkich firmach.
     */
    long getAllUserAmount() {
        long sum = holdings.stream()
                .map(x -> x.getCompanies())
                .flatMap(companies -> companies.stream()
                        .map(x -> x.getUsers())).mapToLong(value -> value.stream().count()).sum();

        return sum;
    }

    /**
     * Zwraca listę wszystkich nazw firm w formie listy. Tworzenie strumienia firm umieść w osobnej metodzie którą
     * później będziesz wykorzystywać.
     */
    List<String> getAllCompaniesNames() {
        List<String> collect = holdings.stream()
                .map(x -> x.getCompanies())
                .flatMap(companies -> companies.stream()
                        .map(Company::getName)).collect(Collectors.toList());
        System.out.println(collect);
        return collect;
    }

    Stream<List<Company>> createCompanies() {
        Stream<List<Company>> listStream = holdings.stream().map(Holding::getCompanies);
        return listStream;
    }

    /**
     * Zwraca listę wszystkich firm jako listę, której implementacja to LinkedList. Obiektów nie przepisujemy po zakończeniu
     * działania strumienia.
     */
    LinkedList<String> getAllCompaniesNamesAsLinkedList() {
        LinkedList<String> collect = createCompanies()
                .flatMap(companies -> companies.stream()
                        .map(Company::getName)).collect(Collectors.toCollection(LinkedList::new));
        return collect;
    }

    /**
     * Zwraca listę firm jako string gdzie poszczególne firmy są oddzielone od siebie znakiem "+"
     */
    String getAllCompaniesNamesAsString() {
        return holdings.stream()
                .map(x -> x.getCompanies())
                .flatMap(companies -> companies.stream()
                        .map(x -> x.getName())).collect(Collectors.joining("+"));
    }

    /**
     * Zwraca listę firm jako string gdzie poszczególne firmy są oddzielone od siebie znakiem "+".
     * Używamy collect i StringBuilder.
     * <p>
     * UWAGA: Zadanie z gwiazdką. Nie używamy zmiennych.
     */
    String getAllCompaniesNamesAsStringUsingStringBuilder() {
        return holdings
                .stream()
                .map(x -> x.getCompanies())
                .flatMap(companies -> companies.stream()
                        .map(x -> x.getName()))
                .collect(Collectors.joining(new StringBuilder("+").toString()));
//                .collect(Collector.of(StringBuilder::new,
//                        (stringBuilder, x) -> stringBuilder.append(x).append("+"),
//                        StringBuilder::append, StringBuilder::toString));


        //.collect(Collector.of(StringBuilder::new,
        //(stringBuilder1, x) -> stringBuilder1.append(x).append("+"),
        //StringBuilder::append, StringBuilder::toString));


    }


    /**
     * Zwraca liczbę wszystkich rachunków, użytkowników we wszystkich firmach.
     */
    long getAllUserAccountsAmount() {
        return getCompanyStream()
                .flatMap(company -> company.getUsers().stream())
                .mapToInt(value -> value.getAccounts().size()).sum();
    }


    /**
     * Zwraca listę wszystkich walut w jakich są rachunki jako string, w którym wartości
     * występują bez powtórzeń i są posortowane.
     */
    String getAllCurrencies() {
        return getCompanyStream()
                .flatMap(company -> company.getUsers().stream())
                .flatMap(user -> user.getAccounts().stream())
                .map(account -> account.getCurrency().toString())
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));
    }

    /**
     * Metoda zwraca analogiczne dane jak getAllCurrencies, jednak na utworzonym zbiorze nie uruchamiaj metody
     * stream, tylko skorzystaj z  Stream.generate. Wspólny kod wynieś do osobnej metody.
     *
     * @see #getAllCurrencies()
     */
    String getAllCurrenciesUsingGenerate() {
        return Stream.generate(Currency::values).limit(4)
                .flatMap(currencies -> Arrays.stream(currencies))
                .map(c -> Objects.toString(c, null))
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));

    }

    private List<String> getAllCurrenciesToListAsString() {
        return getCompanyStream()
                .flatMap(company -> company.getUsers().stream())
                .flatMap(user -> user.getAccounts().stream())
                .map(Account::getCurrency)
                .map(c -> Objects.toString(c, null))
                .collect(Collectors.toList());
    }

    /**
     * Zwraca liczbę kobiet we wszystkich firmach. Powtarzający się fragment kodu tworzący strumień uzytkowników umieść w osobnej
     * metodzie. Predicate określający czy mamy doczynienia z kobietą i niech będzie polem statycznym w klasie.
     */
    long getWomanAmount() {

        return getCompanyStream()
                .flatMap(company -> company.getUsers().stream())
                .filter(user -> user.getSex().equals(Sex.WOMAN)).count();

    }


    /**
     * Przelicza kwotę na rachunku na złotówki za pomocą kursu określonego w enum Currency.
     */
    BigDecimal getAccountAmountInPLN(final Account account) {
        return account
                .getAmount()
                .multiply(BigDecimal.valueOf(account.getCurrency().rate))
                .round(new MathContext(4, RoundingMode.HALF_UP));


        /*Float rate = getAccountStream()
                .map(account1 -> account1.getCurrency().rate).findFirst().get();
        System.out.println("rate is" + rate);


        BigDecimal v = account.getAmount()
                .multiply(BigDecimal.valueOf(rate))
                .round(new MathContext(4, RoundingMode.HALF_UP));

//        BigDecimal value = new BigDecimal(Float.toString(rate));
//        System.out.println("value is " + value);
//        System.out.println("v is " + v);

//        BigDecimal result = value.multiply(v.round(new MathContext(4, RoundingMode.HALF_UP)));*/
//

//        return v; TODO: sprawdzić czemu to nie działa
    }

    /**
     * Przelicza kwotę na podanych rachunkach na złotówki za pomocą kursu określonego w enum Currency  i sumuje ją.
     */
    BigDecimal getTotalCashInPLN(final List<Account> accounts) {
        return accounts.stream()
                .map(account -> account.getAmount().multiply(BigDecimal.valueOf(account.getCurrency().rate)))
                .reduce(BigDecimal::add).get();
    }

    /**
     * Zwraca imiona użytkowników w formie zbioru, którzy spełniają podany warunek.
     */
    Set<String> getUsersForPredicate(final Predicate<User> userPredicate) {
        return getUserStream().filter(userPredicate).map(User::getFirstName).collect(Collectors.toSet());
    }

    /**
     * Metoda filtruje użytkowników starszych niż podany jako parametr wiek, wyświetla ich na konsoli, odrzuca mężczyzn
     * i zwraca ich imiona w formie listy.
     */
    List<String> getOldWoman(final int age) {
        return getUserStream()
                .filter(user -> user.getAge() > age)
                .filter(user -> user.getSex().equals(Sex.MAN))
                .peek(System.out::println)
                .map(User::getFirstName)
                .collect(Collectors.toList());
    }

    /**
     * Dla każdej firmy uruchamia przekazaną metodę.
     */
    void executeForEachCompany(final Consumer<Company> consumer) {
        getCompanyStream().forEach(consumer);
    }

    /**
     * Wyszukuje najbogatsza kobietę i zwraca ja. Metoda musi uzwględniać to że rachunki są w różnych walutach.
     */
    Optional<User> getRichestWoman() {
        return getUserStream()
                .filter(user -> user.getSex().equals(Sex.WOMAN))
                .max(Comparator.comparing(this::getUserAmountInPLN));


    }

    private BigDecimal getUserAmountInPLN(final User user) {
        return user.getAccounts()
                .stream()
                .map(this::getAccountAmountInPLN)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Zwraca nazwy pierwszych N firm. Kolejność nie ma znaczenia.
     */
    Set<String> getFirstNCompany(final int n) {
        Set<String> collect = getCompanyStream()
                .limit(n)
                .map(Company::getName)
                .collect(Collectors.toSet());

        return collect;

    }

    /**
     * Metoda zwraca jaki rodzaj rachunku jest najpopularniejszy. Stwórz pomocniczą metdę getAccountStream.
     * Jeżeli nie udało się znaleźć najpopularnijeszego rachunku metoda ma wyrzucić wyjątek IllegalStateException.
     * Pierwsza instrukcja metody to return.
     */
    AccountType getMostPopularAccountType() {
        return getAccountStream()
                .map(Account::getType)
                .collect(Collectors.groupingBy(accountType -> accountType, Collectors.counting()))
                .entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow(IllegalStateException::new);


    }

    /**
     * Zwraca pierwszego z brzegu użytkownika dla podanego warunku. W przypadku kiedy nie znajdzie użytkownika wyrzuca wyjątek
     * IllegalArgumentException.
     */
    User getUser(final Predicate<User> predicate) {

        return getUserStream().filter(predicate).findFirst().orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Zwraca mapę firm, gdzie kluczem jest jej nazwa a wartością lista pracowników.
     */
    Map<String, List<User>> getUserPerCompany() {
        return getCompanyStream()
                .collect(Collectors.toMap(o -> o.getName(), company -> company.getUsers()));
    }

    /**
     * Zwraca mapę firm, gdzie kluczem jest jej nazwa a wartością lista pracowników przechowywanych jako string
     * składający się z imienia i nazwiska. Podpowiedź:  Możesz skorzystać z metody entrySet.
     */
    Map<String, List<String>> getUserPerCompanyAsString() {
//        getCompanyStream()
//                .collect(Collectors.toMap(o -> o.getName(), company -> company.getUsers()
//                        .stream()
//                        .map(user -> user.getFirstName() + " " + user.getLastName())))
//                .entrySet().stream()
//                .map(Map.Entry::getKey)
//                .map();
        BiFunction<String, String, String> joinNames = (s, s2) -> s + " " + s2;
        Map<String, List<String>> collect = getCompanyStream().collect(Collectors.toMap(o -> o.getName(),
                o -> o.getUsers().stream().map(user -> joinNames.apply(user.getFirstName(), user.getLastName()))
                        .collect(Collectors.toList())));

        return collect;
    }

    private List<String> getUserWithFirstAndLastName() {
        getCompanyStream()
                .flatMap(company -> company.getUsers().stream())
                .map(user -> user.getFirstName() + " " + user.getLastName())
                .collect(Collectors.joining());
        return null;
    }

    /**
     * Zwraca mapę firm, gdzie kluczem jest jej nazwa a wartością lista pracowników przechowywanych jako obiekty
     * typu T, tworzonych za pomocą przekazanej funkcji.
     */
    <T> Map<String, List<T>> getUserPerCompany(final Function<User, T> converter) {
        return getCompanyStream()
                .collect(Collectors.toMap(company -> company.getName(),
                        o -> o.getUsers().stream().map(converter)
                                .collect(Collectors.toList())));
    }

    /**
     * Zwraca mapę gdzie kluczem jest flaga mówiąca o tym czy mamy do czynienia z mężczyzną, czy z kobietą.
     * Osoby "innej" płci mają zostać zignorowane. Wartością jest natomiast zbiór nazwisk tych osób.
     */
    Map<Boolean, Set<String>> getUserBySex() {
        Predicate<User> isManOrWoman = m -> m.getSex() == Sex.WOMAN || m.getSex() == Sex.MAN;
        return getUserStream().filter(isManOrWoman)
                .collect(Collectors.partitioningBy(isWoman, Collectors.mapping(User::getLastName, Collectors.toSet())));

    }

    /**
     * Zwraca mapę rachunków, gdzie kluczem jesy numer rachunku, a wartością ten rachunek.
     */
    Map<String, Account> createAccountsMap() {
        Map<String, Account> collect = getAccountStream().collect(Collectors.toMap(o -> o.getNumber(), o -> o));
        return collect;
    }

    /**
     * Zwraca listę wszystkich imion w postaci Stringa, gdzie imiona oddzielone są spacją i nie zawierają powtórzeń.
     */
    String getUserNames() {
        String collect = getUserStream().distinct().map(User::getFirstName).sorted().collect(Collectors.joining(" "));
        System.out.println(collect);
        return collect;
    }

    /**
     * zwraca zbiór wszystkich użytkowników. Jeżeli jest ich więcej niż 10 to obcina ich ilość do 10.
     */
    Set<User> getUsers() {
        return getUserStream().limit(10).collect(Collectors.toSet());
    }

    /**
     * Zapisuje listę numerów rachunków w pliku na dysku, gdzie w każda linijka wygląda następująco:
     * NUMER_RACHUNKU|KWOTA|WALUTA
     * <p>
     * Skorzystaj z strumieni i try-resources.
     */
    void saveAccountsInFile(final String fileName) {
        //moje rozwiazanie przeszło test
        String collect = getAccountStream().map(account -> account.getNumber() + "|" + account.getAmount() + "|" + account.getCurrency())
                .collect(Collectors.joining());


        try {
            Files.write(Paths.get("accounts.txt"), collect.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //rozwiazanie ze strony
        /*try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            Files.write(Paths.get(String.valueOf(lines)), (Iterable<String>) getAccountStream()
                    .map(account -> account.getNumber() + "|" + account.getAmount() + "|" + account.getCurrency())
                    ::iterator);

        } catch (IOException e) {
            throw new IllegalArgumentException("not implemented yet");
        }
*/

//        throw new IllegalArgumentException("not implemented yet");
    }

    /**
     * Zwraca użytkownika, który spełnia podany warunek.
     */
    Optional<User> findUser(final Predicate<User> userPredicate) {
        Optional<User> first = getUserStream().filter(userPredicate).findFirst();
        return first;
    }

    /**
     * Dla podanego użytkownika zwraca informacje o tym ile ma lat w formie:
     * IMIE NAZWISKO ma lat X. Jeżeli użytkownik nie istnieje to zwraca text: Brak użytkownika.
     * <p>
     * Uwaga: W prawdziwym kodzie nie przekazuj Optionali jako parametrów.
     */
    String getAdultantStatus(final Optional<User> user) {
//      return   user.flatMap(user1 -> getUserStream().filter(user2->Objects.equals(user2, user1)).findFirst())
//                .map(u -> System.out.format("%s %s ma lat %d", u.getFirstName(), u.getLastName(), u.getAge()));
//                .orElse("Brak użytkownika");


        return "";

    }

    /**
     * Metoda wypisuje na ekranie wszystkich użytkowników (imie, nazwisko) posortowanych od z do a.
     * Zosia Psikuta, Zenon Kucowski, Zenek Jawowy ... Alfred Pasibrzuch, Adam Wojcik
     */
    void showAllUser() {
        getUserStream()
                .map(user -> user.getFirstName() + " " + user.getLastName() + ", ")
                .sorted(Comparator.reverseOrder()).forEach(System.out::print);

    }

    /**
     * Zwraca mapę, gdzie kluczem jest typ rachunku a wartością
     * kwota wszystkich środków na rachunkach tego typu przeliczona na złotówki.
     */
    Map<AccountType, BigDecimal> getMoneyOnAccounts() {

        return getAccountStream()
                .collect(Collectors.toMap(o -> o.getType(), o -> o.getAmount()
                                .multiply(BigDecimal.valueOf(o.getCurrency().rate))
                                .round(new MathContext(6, RoundingMode.DOWN)),
                        BigDecimal::add));

    }

    /**
     * Zwraca sumę kwadratów wieków wszystkich użytkowników.
     */
    int getAgeSquaresSum() {
        int sum = getUserStream()
                .mapToInt(user -> (int) Math.pow(user.getAge(), 2)).sum();

        return sum;
    }

    /**
     * Metoda zwraca N losowych użytkowników (liczba jest stała).
     * Skorzystaj z metody generate. Użytkownicy nie mogą się powtarzać, wszystkie zmienną
     * muszą być final. Jeżeli podano liczbę większą niż liczba użytkowników należy wyrzucić
     * wyjątek (bez zmiany  sygnatury metody).
     */
    List<User> getRandomUsers(final int n) {

        return Optional.of(Stream.generate(this::getUsers)
                .flatMap(Collection::stream)
                .limit(n).distinct().collect(Collectors.toList()))
                .orElseThrow(ArrayIndexOutOfBoundsException::new);
    }

    /**
     * 38.
     * Stwórz mapę gdzie kluczem jest typ rachunku a wartością mapa mężczyzn posiadających ten rachunek, gdzie kluczem jest
     * obiekt User a wartoscią suma pieniędzy na rachunku danego typu przeliczona na złotkówki.
     */


    public Map<Stream<AccountType>, Map<User, BigDecimal>> createMapWithUserAsManWithAccountInPln() {
        return getCompanyStream().collect(Collectors.toMap(o -> o.getUsers().stream()
                .flatMap(user -> user.getAccounts().stream().map(account -> account.getType())), this::getUserWhichIsMan));

    }

    private Map<User, BigDecimal> getUserWhichIsMan(Company company) {
        Map<User, BigDecimal> collect = company.getUsers().stream()
                .filter(isWoman).collect(Collectors.toMap(Function.identity(), this::countAccountUserWithPLN));
        return collect;
    }

    private BigDecimal countAccountUserWithPLN(User user) {
        return user.getAccounts().stream().map(this::getAccountAmountInPLN).reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    /**
     * 39. Policz ile pieniędzy w złotówkach jest na kontach osób które nie są ani kobietą ani mężczyzną.
     */

    public BigDecimal moneyOnAccountsInPersonWhichIsNotWomenAndNotMen(Company company) {
        return company.getUsers().stream().filter(o -> o.getSex().equals(Sex.OTHER))
                .map(this::getUserAmountInPLN).reduce(BigDecimal.ZERO, BigDecimal::add).round(MathContext.DECIMAL32);
    }


    /**
     * Zwraca strumień wszystkich firm.
     */
    private Stream<Company> getCompanyStream() {
        return holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream());
    }

    /**
     * Zwraca zbiór walut w jakich są rachunki.
     */
    private Set<Currency> getCurenciesSet() {
        return null;
    }

    /**
     * Tworzy strumień rachunków.
     */
    private Stream<Account> getAccountStream() {
        return getCompanyStream()
                .flatMap(company -> company.getUsers().stream())
                .flatMap(user -> user.getAccounts().stream());
    }

    /**
     * Tworzy strumień użytkowników.
     */
    private Stream<User> getUserStream() {
        return getCompanyStream()
                .flatMap(company -> company.getUsers().stream());
    }

    public static void main(String[] args) {
        WorkShop workShop = new WorkShop();
//        System.out.println(workShop.getAllUserAccountsAmount());
//        final Account accountWithOneZloty = Account.builder()
//                .amount(new BigDecimal("3.72"))
//                .currency(Currency.PLN)
//                .build();
//        workShop.getAccountAmountInPLN(accountWithOneZloty);
//        workShop.getRichestWoman();
//        workShop.getUserPerCompanyAsString();
        workShop.getUserNames();
    }

}