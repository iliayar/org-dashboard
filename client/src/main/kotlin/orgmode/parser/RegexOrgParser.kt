package orgmode.parser

import orgmode.*

class RegexOrgParser(src: Source) : AbstractParser<Org>(src) {

    val tableRegex:        Regex = """^(\s*)\|((.*)\|)+""".toRegex()
    val tableSplitRegex:   Regex = """^(\s*)\|(\-+\+)+\-+\|""".toRegex()

    val linkRegex:         Regex = """(.*)(\[\[([^\]]+)\](\[(.*)\])?\])(.*(\n)?)""".toRegex()
    val emphasisRegex:     Regex = """(.*)(^|\s)(\*([^ ].+[^ ]|[^ ])\*)(\s|$)(.*(\n)?)""".toRegex()
    val strikeoutRegex:    Regex = """(.*)(^|\s)(\+([^ ].+[^ ]|[^ ])\+)(\s|$)(.*(\n)?)""".toRegex()
    val underlineRegex:    Regex = """(.*)(^|\s)(\_([^ ].+[^ ]|[^ ])\_)(\s|$)(.*(\n)?)""".toRegex()
    val codeRegex:         Regex = """(.*)(^|\s)(\=([^ ].+[^ ]|[^ ])\=)(\s|$)(.*(\n)?)""".toRegex()
    val italicRegex:       Regex = """(.*)(^|\s)(\/([^ ].+[^ ]|[^ ])\/)(\s|$)(.*(\n)?)""".toRegex()
    val statisticRegex:    Regex = """(.*)(\[[0-9]*\/[0-9]*\]|\[[0-9]{0,2}%\])(.*(\n)?)""".toRegex()
    val textRegex:         Regex = """(.*)(\n)?""".toRegex()
    val sectionRegex:      Regex = """^(\*+) ((TODO|FIXME|DONE) )?(.+(\n)?)""".toRegex()
    val checkboxListRegex: Regex = """^(\s*)(\+|-|[0-9]+[\.\)])\s+(\[@(\d+)\]\s+)?(\[([X \-])\](\s|$))(.*(\n)?)""".toRegex()
    val listRegex:         Regex = """^(\s*)(\+|-|[0-9]+[\.\)])\s+(\[@(\d+)\]\s+)?(.*(\n)?)""".toRegex()
    val blockBeginRegex:   Regex = """^(\s*)#\+BEGIN_(SRC)(.*(\n)?)?""".toRegex(RegexOption.IGNORE_CASE)
    val blockEndRegex:     Regex = """^(\s*)#\+END_(SRC)(\n|$)""".toRegex(RegexOption.IGNORE_CASE)
    val planningRegex:     Regex = """^\s*(DEADLINE|SCHEDULED|CLOSED): (.+)(\n)?""".toRegex()
    val propertyRegex:     Regex = """^:([^\s]+(\+)?):( (.+))?""".toRegex()
    val keywordRegex:      Regex = """^#\+([^ ]+): (.*)""".toRegex()

    val activeTimestampRegex:   Regex = """<(\d{4})-(\d{2})-(\d{2}) ([^\+>\]\-\n0-9]+)( ((\d{1,2}):(\d{2}))(-((\d{1,2}):(\d{2})))?)?( (\+|\+\+|\.\+|-|--)(\d+)([hdwmy])){0,2}>(.*)""".toRegex()
    val inactiveTimestampRegex: Regex = """\[(\d{4})-(\d{2})-(\d{2}) ([^\+>\]\-\n0-9]+)( ((\d{1,2}):(\d{2}))(-((\d{1,2}):(\d{2})))?)?( (\+|\+\+|\.\+|-|--)(\d+)([hdwmy])){0,2}\](.*)""".toRegex()

    var buffer: String? = null

    fun getIndent(line: String): Int {
        var i: Int = 0

        while(i < line.length && line[i].isWhitespace()) {
            i++
        }
        return i
    }

    override fun parse(): Org {

        var doc: OrgDocument = OrgDocument()

        parseSection(doc)

        return doc
    }

    fun parseSection(section: Section): Section? {
        var paragraph: Paragraph = Paragraph()
        var skip: Boolean
        var line: Org?
        var indent: Int?
        var rawLine: String

        // Trying parse planning properties
        rawLine = getLine()
        var planning = planningRegex.matchEntire(rawLine)
        while(planning != null) {
            var match: MatchResult? = activeTimestampRegex.matchEntire(planning.groups[2]!!.value)
            var active: Boolean = true
            if(match == null) {
                active = false
                match = inactiveTimestampRegex.matchEntire(planning.groups[2]!!.value)
                if(match == null) throw ParserException("Wrong timestamp format: ${planning.groups[2]!!.value}")
            }
            section.plan(Planning(when(planning.groups[1]!!.value) {
                             "DEADLINE"  -> PLANNING_TYPE.DEADLINE
                             "SCHEDULED" -> PLANNING_TYPE.SCHEDULED
                             "CLOSED"    -> PLANNING_TYPE.CLOSED
                             else -> throw ParserException("Unknown planning type")
            }, parseTimestamp(match, active)))
            planning = planningRegex.matchEntire(match.groups[17]!!.value)
            if(planning != null) {
                rawLine = match.groups[17]!!.value
                continue
            }
            rawLine = getLine()
            planning = planningRegex.matchEntire(rawLine)
        }

        // Trying parse properties
        var propertyMatch = propertyRegex.matchEntire(rawLine)
        while(propertyMatch != null) {
            var property = Property(propertyMatch.groups[1]!!.value,
                                    propertyMatch.groups[2] != null,
                                    propertyMatch.groups[4]?.value
            )
            if(property.name == "END") {
                rawLine = getLine()
                break
            }
            if(src.isEof()) throw ParserException("Preporties has not END")
            if(property.name != "PROPERTIES") section.addProperty(property)
            rawLine = getLine()
            propertyMatch = propertyRegex.matchEntire(rawLine)
        }
        skip = true
        indent = getIndent(rawLine)
        line = parseLine(rawLine)


        // Parsing inner headlines
        while (skip || !src.isEof()) {
            if (!skip) {
                rawLine = getLine()
                indent = getIndent(rawLine)
                line = parseLine(rawLine)
            }
            skip = false

            indent ?: throw ParserException("Wrong skip")
            line ?: throw ParserException("Wrong skip")

            if(line is OrgTableLine) {
                if(paragraph !is OrgTable) {
                    if (!paragraph.isEmpty()) section.add(paragraph)
                    paragraph = OrgTable(emptyList())
                }
                paragraph.add(line)
            } else if (line is MarkupText) {
                if (!line.isEmpty()) {
                    paragraph.add(line)
                } else if (!paragraph.isEmpty()) {
                    section.add(paragraph)
                    paragraph = Paragraph()
                }
            } else if (line is Section) {
                if (!paragraph.isEmpty()) section.add(paragraph)
                if (line.level <= section.level) {
                    return line
                } else {
                    var nextSection: Section? = line
                    while (nextSection != null && nextSection.level > section.level) {
                        var tempSection: Section? = parseSection(nextSection)
                        section.add(nextSection)
                        nextSection = tempSection
                    }
                    return nextSection
                }
            } else if (line is ListEntry) {
                if (!paragraph.isEmpty()) section.add(paragraph)
                paragraph = Paragraph()
                var list = OrgList()
                val (newLine, newIndent) = parseList(list, line, indent)
                section.add(list)
                if (newLine == null) {
                    continue
                }
                line = newLine
                indent = newIndent
                skip = true
            } else if(line is Block) {
                if (!paragraph.isEmpty()) section.add(paragraph)
                paragraph = Paragraph()
                parseBlock(line)
                section.add(line)
            }
        }
        if (!paragraph.isEmpty()) {
            section.add(paragraph)
        }
        return null
    }

    fun parseBlock(block: Block) {
        while(true) {
            var codeLine = getLine()
            if(blockEndRegex.matches(codeLine)) {
                return
            }
            block.add(codeLine)
            if(src.isEof()) throw ParserException("Code block without end")
        }
    }

    fun parseList(list: OrgList, firstEntry: ListEntry, curIndent: Int): Pair<Org?, Int> {

        var paragraph: Paragraph = Paragraph()
        var entry = firstEntry
        var skip: Boolean = false
        var line: Org? = null
        var indent: Int? = null
        var emptyLines: Int = 0

        while (!src.isEof()) {

            if (!skip) {
                indent = skipWhitespaces()
                line = parseLine(" ".repeat(indent) + getLine())
            }

            skip = false

            indent ?: throw ParserException("Wrong skip")
            line ?: throw ParserException("Wrong skip")

            if (line is Section) {
                if (!paragraph.isEmpty()) entry.add(paragraph)
                list.add(entry)
                return Pair(line, 0)
            } else if(line is OrgTableLine) {
                emptyLines = 0
                if (indent <= curIndent) {
                    if (!paragraph.isEmpty()) entry.add(paragraph)
                    list.add(entry)
                    return Pair(line, indent)
                } else {
                    if(paragraph !is OrgTable) {
                        if (!paragraph.isEmpty()) entry.add(paragraph)
                        paragraph = OrgTable(emptyList())
                    }
                    paragraph.add(line)
                }
            } else if (line is ListEntry) {
                if (!paragraph.isEmpty()) entry.add(paragraph)
                paragraph = Paragraph()
                if (indent < curIndent) {
                    list.add(entry)
                    return Pair(line, indent)
                } else if (indent == curIndent) {
                    list.add(entry)
                    entry = line
                } else {
                    var newList = OrgList()
                    val (nextLine, newIndent) = parseList(newList, line, indent)
                    entry.add(newList)
                    if (nextLine == null) {
                        list.add(entry)
                        return Pair(null, 0)
                    }
                    line = nextLine
                    indent = newIndent
                    skip = true
                }
            } else if (line is MarkupText) {
                if (line.isEmpty()) {
                    if (!paragraph.isEmpty()) entry.add(paragraph)
                    paragraph = Paragraph()
                    emptyLines++
                    if (emptyLines >= 2) {
                        list.add(entry)
                        return Pair(null, 0)
                    }
                    continue
                }
                emptyLines = 0
                if (indent <= curIndent) {
                    if (!paragraph.isEmpty()) entry.add(paragraph)
                    list.add(entry)
                    return Pair(line, indent)
                } else {
                    if(!line.isEmpty()) paragraph.add(line)
                }
            } else if(line is Block) {
                if (!paragraph.isEmpty()) entry.add(paragraph)
                paragraph = Paragraph()
                parseBlock(line)
                entry.add(line)
            }
        }
        if (!paragraph.isEmpty()) entry.add(paragraph)
        list.add(entry)

        return Pair(null, 0)
    }

    fun parseLine(line: String): Org {
        var match: MatchResult? = sectionRegex.matchEntire(line)
        if (match != null) {
            var state: STATE = STATE.NONE
            if(match.groups[3] != null) {
                state = when(match.groups[3]!!.value) {
                    "TODO"  -> STATE.TODO
                    "FIXME" -> STATE.FIXME
                    "DONE"  -> STATE.DONE
                    else    -> throw ParserException("Unknown state word")
                }
            }
            return Section(MarkupText(parseMarkup(match.groups[4]!!.value)), match.groups[1]!!.value.length, state = state)
        }
        match = checkboxListRegex.matchEntire(line)
        if (match != null) {
            var state = when(match.groups[6]!!.value) {
                    "X"  -> LIST_CHECKBOX.CHECKED
                    "-"  -> LIST_CHECKBOX.PARTIAL_CHECKED
                    " "  -> LIST_CHECKBOX.UNCHECKED
                    else -> throw ParserException("Unknown list checked state")
            }
            return ListEntry(MarkupText(parseMarkup(match.groups[8]?.value ?: "")),
                             match.groups[2]!!.value,
                             match.groups[1]?.value?.length ?: 0,
                             checkbox = state,
                             counter = match.groups[4]?.value?.toInt())
        }
        match = listRegex.matchEntire(line)
        if (match != null) {
            return ListEntry(MarkupText(parseMarkup(match.groups[5]?.value ?: "")),
                             match.groups[2]!!.value,
                             match.groups[1]?.value?.length ?: 0,
                             counter = match.groups[4]?.value?.toInt())
        }
        match = keywordRegex.matchEntire(line)
        if(match != null) {
            return Keyword(match.groups[1]!!.value, match.groups[2]!!.value)
        }
        match = blockBeginRegex.matchEntire(line)
        if(match != null) {
            if(match.groups[2]!!.value == "SRC") {
                return CodeBlock();
            }
        }
        match = tableSplitRegex.matchEntire(line)
        if(match != null) {
            return OrgTableSplit(match.groups[0]!!.value.split("+").size)
        }
        match = tableRegex.matchEntire(line)
        if(match != null) {
            var cols: List<String> = match.groups[0]!!.value.split("|")
            cols = cols.subList(1, cols.size - 1)
            var tableLine: OrgTableLine = OrgTableLine(emptyList())
            for(col: String in cols) {
                tableLine.add(MarkupText(parseMarkup(col)))
            }
            return tableLine;
        }

        return MarkupText(parseMarkup(line))
    }

    fun parseNextMarkup(match: MatchResult, headId: Int, markup: MarkupText, restId: Int): List<MarkupText> {
        var res: List<MarkupText> = listOf()

        if (match.groups[headId] != null && match.groups[headId]!!.value.trim(' ') != "") {
            res += parseMarkup(match.groups[headId]!!.value)
        }
        res += markup
        if (match.groups[restId] != null && match.groups[restId]!!.value.trim(' ') != "") {
            res += parseMarkup(match.groups[restId]!!.value)
        }
        return res
    }

    fun generalMarkup(ctor: (List<MarkupText>, MarkupText?) -> MarkupText): (MatchResult) -> List<MarkupText> {
        return {
            match ->
            parseNextMarkup(match, 1, ctor(parseMarkup(match.groups[4]!!.value), null), 6)
        }
    }

    val regexToMarkup: Map<Regex, (MatchResult) -> List<MarkupText>> = mapOf(
        codeRegex to {
            match ->
            parseNextMarkup(match, 1, Code(match.groups[4]!!.value), 6)
        },
        linkRegex to {
            match ->
            var link: Link
            if (match.groups[5] != null) {
                link = Link(match.groups[3]!!.value, parseMarkup(match.groups[5]!!.value))
            } else {
                link = Link(match.groups[3]!!.value)
            }
            parseNextMarkup(match, 1, link, 6)
        },
        statisticRegex to {
            match ->
            parseNextMarkup(match, 1, StatisticCookie(match.groups[2]!!.value), 3)
        },
        italicRegex to generalMarkup(::Italic),
        emphasisRegex to generalMarkup(::Emphasis),
        strikeoutRegex to generalMarkup(::Strikeout),
        underlineRegex to generalMarkup(::Underline),
        textRegex to {
            match ->
            var res: List<MarkupText> = listOf()
            if (match.groups[1]!!.value.trim(' ') != "") {
                res += Text(match.groups[1]!!.value)
            }
            if (match.groups[2] != null) {
                res += LineBreak()
            }
            res
        }
    )

    fun parseTimestamp(match: MatchResult, active: Boolean): Timestamp {
        return Timestamp(
            active,
            match.groups[1]!!.value.toInt(),
            match.groups[2]!!.value.toInt(),
            match.groups[3]!!.value.toInt(),
            match.groups[4]!!.value,
            match.groups[7]?.value?.toInt(),
            match.groups[8]?.value?.toInt(),
            match.groups[11]?.value?.toInt(),
            match.groups[12]?.value?.toInt(),
            match.groups[14]?.value,
            match.groups[15]?.value?.toInt(),
            match.groups[16]?.value?.get(0)
        )
    }

    fun getLine(): String {
        var res: StringBuilder = StringBuilder()

        while (!test('\n') && !src.isEof()) {
            if (test('\\')) {
                if (test('\\')) {
                    if (test('\n')) {
                        if (res.isEmpty()) break
                        res.append("\n")
                        break
                    }
                    res.append("\\")
                } else {
                    res.append(src.getChar())
                    src.nextChar()
                    continue
                }
            }
            res.append(src.getChar())
            src.nextChar()
        }

        return res.toString()
    }

    fun parseMarkup(s: String): List<MarkupText> {
        for ((regex, getMarkup) in regexToMarkup) {
            var match: MatchResult? = regex.matchEntire(s)
            if (match != null) {
                // if(match.groups[0]!!.value == "") return listOf()
                return getMarkup(match)
            }
        }

        throw ParserException("Not found any matched group")
    }
}
