import io.vavr.Function1
import io.vavr.PartialFunction
import io.vavr.collection.HashSet
import io.vavr.collection.List
import io.vavr.control.Option
import spock.lang.Specification

import java.util.function.Function
import java.util.function.Supplier

import static java.util.Objects.nonNull

/**
 * Created by mtumilowicz on 2019-03-02.
 */
class Workshop extends Specification {

    def "create empty option"() {
        given:
        Option none = -1 // create here, hint: none()

        expect:
        none.isEmpty()
    }

    def "create not empty option with not null value"() {
        given:
        Option<Integer> some = -1 // create here, hint: some()

        expect:
        some.isDefined()
        some.get()
    }

    def "create not empty option with null value"() {
        given:
        Option<Integer> some = -1 // create here, hint: some()

        expect:
        some.isDefined()
        !some.get()
    }

    def "check if option is empty / not empty"() {
        given:
        def empty = Option.none()
        def notEmpty = Option.some()

        expect:
        !empty // check here
        !notEmpty // check here
    }

    def "conversion: optional -> option"() {
        given:
        Optional<Integer> emptyOptional = Optional.empty()
        Optional<Integer> notEmptyOptional = Optional.of(1)

        when:
        Option<Integer> emptyOption = emptyOptional // convert here to option, hint: ofOptional
        Option<Integer> notEmptyOption = notEmptyOptional// convert here to option, hint: ofOptional

        then:
        emptyOption == Option.none()
        notEmptyOption == Option.some(1)
    }

    def "conversion: option -> optional"() {
        given:
        Option<Integer> emptyOption = Option.none()
        Option<Integer> notEmptyOption = Option.some(1)

        when:
        Optional<Integer> emptyOptional = emptyOption // convert here to optional, hint: toJavaOptional
        Optional<Integer> notEmptyOptional = notEmptyOption // convert here to optional, hint: toJavaOptional
        then:
        emptyOptional == Optional.empty()
        notEmptyOptional == Optional.of(1)
    }

    def "conversion: sum values of options: sum = Some(sum all option.get) or None if any of option is empty"() {
        given:
        Option<Integer> value1 = Option.some(1)
        Option<Integer> value2 = Option.some(3)
        Option<Integer> value3 = Option.some(5)
        Option<Integer> value4 = Option.none()

        and:
        List<Option<Integer>> valuesToValue3 = List.of(value1, value2, value3)
        List<Option<Integer>> valuesToValue4 = List.of(value1, value2, value3, value4)

        when:
        Option<Number> valuesToValue3Sum = -1 // sum here, hint: sequence
        Option<Number> valuesToValue4Sum = -1 // sum here, hint: sequence

        then:
        valuesToValue3Sum.defined
        valuesToValue3Sum.get() == 9
        valuesToValue4Sum == Option.none()
    }

    def "load additional data only when person has age > 18"() {
        given:
        def adult = new Person(25)
        def kid = new Person(10)
        Supplier<AdditionalData> loader = { new AdditionalData() }

        when:
        Option<AdditionalData> forAdult = -1 // convert here, hint: when()
        Option<AdditionalData> forKid = -1 // convert here, hint: when()

        then:
        forAdult.isDefined()
        forAdult.get().data == 'additional data'
        forKid.isEmpty()
    }

    def "map value with a partial function; if not defined -> Option.none()"() {
        given:
        Option<Integer> zero = Option.some(0)

        and:
        PartialFunction<Integer, Integer> div = Function1.of({ 5 / it })
                .partial({ it != 0 })
        PartialFunction<Integer, Integer> add = Function1.of({ 5 + it })
                .partial({ true })

        when:
        Option<Integer> dived = zero // convert here, hint: collect, use div()
        Option<Integer> summed = zero // convert here, hint: collect, use add()

        then:
        dived == Option.none()
        summed == Option.some(5)
    }

    def "if empty - do action, otherwise do nothing"() {
        given:
        Option<Integer> empty = Option.none()
        Option<Integer> notEmpty = Option.some(5)

        and:
        def counter = 0

        and:
        Runnable action = { counter++ }

        when:
        empty // perform action here, hint: onEmpty()
        notEmpty // perform action here, hint: onEmpty()

        then:
        counter.get() == 1
    }

    def "if option has an adult as a value do nothing, otherwise empty"() {
        given:
        Option<Person> adult = Option.some(new Person(20))
        Option<Person> kid = Option.some(new Person(15))

        when:
        Option<Person> checkedAdult = Option.none() // filter here
        Option<Person> checkedKid = Option.some() // filter here

        then:
        checkedAdult == adult
        checkedKid == Option.none()
    }

    def "find in cache, otherwise try to find in the database, otherwise empty"() {
        given:
        def fromCacheId = 1
        def fromDatabaseId = 2
        def fakeId = 3

        when:
        def fromCache = Repository.findById(fromCacheId)
        def fromDatabase = Repository.findById(fromDatabaseId)
        def notFound = Repository.findById(fakeId)

        then:
        Option.some('from cache') == fromCache
        Option.some('from database') == fromDatabase
        Option.none() == notFound
    }

    def "throw IllegalStateException if option is empty, otherwise get value"() {
        given:
        def empty = Option.none()

        when:
        empty // perform get or throw here

        then:
        thrown(IllegalStateException)
    }

    def "square value then convert to String, if empty - do nothing, null should be treated as 0"() {
        given:
        Option<Integer> defined = Option.some(2)
        Option<Integer> definedNull = Option.some()
        Option<Integer> empty = Option.none()

        when:
        Option<String> definedMapped = defined // map here
        Option<String> definedNullMapped = definedNull // map here, hint: null check!
        Option<String> emptyMapped = empty // map here, hint: null check!

        then:
        definedMapped.defined
        definedMapped.get() == '4'
        definedNullMapped.defined
        definedNullMapped.get() == '0'
        emptyMapped.empty
    }

    def "flatten Option, basics"() {
        given:
        def id = Option.some(1)

        when:
        def found = id // perform mapping on id, use Repository.findById, hint: flatMap

        then:
        found.get() == 'from cache'
    }

    def "flatten Option: find engine for a given car id"() {
        given:
        def existingCarId = 1
        def notExistingCarId = 2

        when:
        Option<Engine> engineFound = Option.none() // find using Repository.findCarById, Repository.findEngineById, hint: flatMap
        Option<Engine> engineNotFound = Option.none() // find using Repository.findCarById, Repository.findEngineById, hint: flatMap

        then:
        engineFound == Option.some(new Engine(1))
        engineNotFound.empty
    }

    def "increment counter by option value"() {
        given:
        Option<Integer> empty = Option.<Integer> none()
        Option<Integer> five = Option.some(5)

        and:
        def counter = 0

        when:
        empty // increment counter here, hint: peek(), forEach()
        five // increment counter here, hint: peek(), forEach()

        then:
        counter == 5
    }

    def "convert: Option<Integer> -> String, Option.none() -> empty string"() {
        given:
        Option<Integer> empty = Option.<Integer> none()
        Option<Integer> five = Option.some(5)
        and:
        Function<Option<Integer>, String> transformer = { it.isEmpty() ? '' : it.get().toString() }

        when:
        def transformedEmpty = empty // perform transformation here, hint: transform()
        def transformerFive = five // perform transformation here, hint: transform()

        then:
        transformedEmpty == ''
        transformerFive == '5'
    }

    def "sum all values in the list"() {
        given:
        def list = List.of(List.of(1, 2, 3), HashSet.of(4, 5), Option.some(7))

        when:
        def sum = list // perform summing here

        then:
        sum == 22
    }

    def "check if somewhere in the list is 7"() {
        given:
        def existing = 7
        def notExisting = 10
        def list = List.of(List.of(1, 2, 3), HashSet.of(4, 5), Option.some(existing))

        when:
        def exists = list // perform searching here
        def notExists = list // perform searching here

        then:
        exists
        !notExists
    }

    def "check if all values in the list are < 10"() {
        given:
        def list = List.of(List.of(1, 2, 3), HashSet.of(4, 5), Option.some(7))

        when:
        def lessThan10 = list // perform action here

        then:
        lessThan10
    }

    def "function composition, monadic law; example of option.map(f g) = option.map(f).map(g)"() {
        given:
        Function<Integer, Integer> nullFunction = { null }
        Function<Integer, String> safeToString = { nonNull(it) ? String.valueOf(it) : 'null' }
        Function<Integer, String> composition = nullFunction.andThen(safeToString)

        expect:
        // Optional map != Optional map map
        // Optional stream map == Optional stream map map
        // Option map == Option map map
        Optional.of(1).map(composition) != Optional.of(1).map(nullFunction).map(safeToString)
        Optional.of(1).stream().map(composition).findAny() == Optional.of(1).stream().map(nullFunction).map(safeToString).findAny()
        Option.of(1).map(composition) == Option.of(1).map(nullFunction).map(safeToString)
    }
}