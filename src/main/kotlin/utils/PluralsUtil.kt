package com.ehedgehog.utils

object PluralsUtil {

    enum class Gender {
        MASCULINE, FEMININE, NEUTER
    }

    data class Plural(
        val one: String,
        val few: String,
        val many: String,
        val gender: Gender
    )

    private val masculineEndings = arrayOf("й", "арь", "тель")
    private val neuterEndings = arrayOf("о", "е", "мя")
    private val feminineEndings = arrayOf('а', 'я', 'ь')

    private val vowels = arrayOf('а', 'о', 'у', 'ы', 'и', 'э', 'е', 'ё', 'ю', 'я')
    private val softeningConsonants = arrayOf('г', 'к', 'х', 'ж', 'ч', 'ш', 'щ')

    private val cachedPlurals = mutableMapOf<String, Plural>()

    fun pluralize(count: Int, noun: String, actionWord: String? = null): String {
        val plurals = getPlurals(noun)

        val genderizedAction = actionWord?.let { genderizeActionWord(it, plurals.gender) } ?: ""

        return when {
            count == 1 -> "$genderizedAction ${plurals.one}".trim()
            count % 100 in 11..14 -> "${actionWord ?: ""} $count ${plurals.many}".trim()
            count % 10 == 1 -> "$genderizedAction $count ${plurals.one}".trim()
            count % 10 in 2..4 -> "${actionWord ?: ""} $count ${plurals.few}".trim()
            else -> "${actionWord ?: ""} $count ${plurals.many}".trim()
        }
    }

    fun getPlurals(noun: String) = cachedPlurals.getOrPut(noun.lowercase()) { generatePlurals(noun) }

    private fun genderizeActionWord(word: String, gender: Gender): String = when (gender) {
        Gender.MASCULINE -> word.dropLast(1)
        Gender.FEMININE -> word.dropLast(1).plus('а')
        Gender.NEUTER -> word
    }

    private fun generatePlurals(noun: String): Plural = when {
        noun.last().toString() in masculineEndings || isEndsWithConsonant(noun) -> masculinePlurals(noun)
        noun.last().toString() in neuterEndings -> neuterPlurals(noun)
        noun.last() in feminineEndings -> femininePlurals(noun)
        else -> Plural("предмет типа $noun", "предмета типа $noun", "предметов типа $noun", Gender.MASCULINE)
    }

    private fun isEndsWithConsonant(noun: String): Boolean {
        return noun.last() !in vowels && !noun.endsWith('ь')
    }

    private fun masculinePlurals(noun: String): Plural = when {
        noun.endsWith('й') -> Plural(
            noun,
            noun.dropLast(1).plus('я'),
            noun.dropLast(1).plus("ев"),
            Gender.MASCULINE
        )

        noun.endsWith('ь') -> Plural(
            noun,
            noun.dropLast(1).plus('я'),
            noun.dropLast(1).plus("ей"),
            Gender.MASCULINE
        )

        else -> Plural(noun, noun.plus('а'), noun.plus("ов"), Gender.MASCULINE)
    }

    private fun neuterPlurals(noun: String): Plural = when {
        noun.endsWith("мя") -> Plural(
            noun,
            noun.dropLast(1).plus("ени"),
            noun.dropLast(1).plus("ен"),
            Gender.NEUTER
        )

        noun.endsWith('е') -> {
            val withoutEnding = noun.dropLast(1)
            val thirdForm = when {
                isEndsWithConsonant(withoutEnding) -> noun.plus('й')
                withoutEnding.endsWith('ь') -> noun.plus('в')
                else -> withoutEnding.plus('й')
            }
            Plural(noun, noun.dropLast(1).plus('я'), thirdForm, Gender.NEUTER)
        }

        else -> {
            val withoutEnding = noun.dropLast(2)
            val beforeLast = noun.getOrNull(noun.lastIndex - 1)
            val thirdForm = when {
                noun == "очко" -> "очков"
                isEndsWithConsonant(withoutEnding) -> {
                    withoutEnding.plus("е$beforeLast")
                }
                withoutEnding.endsWith('ь') -> withoutEnding.dropLast(1).plus("е$beforeLast")
                else -> noun.dropLast(1)
            }
            Plural(noun, noun.dropLast(1).plus('а'), thirdForm, Gender.NEUTER)
        }
    }

    private fun femininePlurals(noun: String): Plural = when {
        noun.endsWith('ь') -> Plural(
            noun,
            noun.dropLast(1).plus('и'),
            noun.dropLast(1).plus("ей"),
            Gender.FEMININE
        )

        noun.endsWith("ка") -> {
            val thirdFormEnding = if (noun.getOrNull(noun.lastIndex - 2) in softeningConsonants) "ек" else "ок"
            Plural(
                noun,
                noun.dropLast(1).plus('и'),
                noun.dropLast(2).plus(thirdFormEnding),
                Gender.FEMININE
            )
        }

        noun.endsWith('а') -> {
            val secondFormEnding = if (noun.getOrNull(noun.lastIndex - 1) in softeningConsonants) 'и' else 'ы'
            Plural(
                noun,
                noun.dropLast(1).plus(secondFormEnding),
                noun.dropLast(1),
                Gender.FEMININE
            )
        }

        else -> {
            val thirdForm = when {
                noun.getOrNull(noun.lastIndex - 1) in vowels -> noun.dropLast(1).plus('й')
                noun.getOrNull(noun.lastIndex - 1) == 'ь' -> noun.dropLast(2).plus("ей")
                noun.endsWith("шня") -> noun.dropLast(2).plus("ен")
                noun.endsWith("ля") -> noun.dropLast(2).plus("ель")
                else -> noun.dropLast(2).plus("ень")
            }

            Plural(noun, noun.dropLast(1).plus('и'), thirdForm, Gender.FEMININE)
        }
    }

}