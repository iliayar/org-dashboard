package orgmode.parser

import orgmode.*
import orgmode.utils.*

class OrgParser(src: Source) : AbstractParser<Org>(src) {

    var markup_symbols: List<Char> = listOf('*', '/', '+', '=', '_', ']')
    var raw_markup: StringBuilder = StringBuilder()

    override fun parse(): Org {
        var root: Section = OrgDocument()
        parseSection(root)
        return root
    }

    fun parseSection(root: Section): Section? {

        var lines: List<MarkupText> = emptyList()

        var skip: Boolean = false

        var element: Org = Text("No way")

        while (!src.isEof()) {

            if (!skip) {
                element = parseLine()
            }
            skip = false

            if (element is Section) {
                if (!lines.isEmpty()) root.add(Paragraph(lines))

                if (element.level > root.level) {
                    var section: Section? = parseSection(element)
                    while (true) {
                        root.add(element)

                        if (section == null || section.level <= root.level) {
                            return section
                        }

                        element = section
                        section = parseSection(element)
                    }
                } else return element
            } else if (element is ListEntry) {
                if (!lines.isEmpty()) {
                    root.add(Paragraph(lines))
                    lines = emptyList()
                }
                var (newElement, _, list, _) = parseList(element)
                root.add(list)
                skip = true
                element = newElement
            } else if (element is MarkupText && element.isEmpty()) {
                if (!lines.isEmpty()) {
                    root.add(Paragraph(lines))
                    lines = emptyList()
                }
            } else if (element is MarkupText) {
                lines += element
            } else {
                throw ParserException("Unknown type when parsenf section")
            }
        }
        if (!lines.isEmpty()) root.add(Paragraph(lines))
        return null
    }

    fun parseLine(): Org {

        if (test('*')) {
            return tryParseHeader()
        }

        val indent: Int = skipWhitespaces()
        val c: Char = src.getChar()
        if (testRange('0'..'9') || test('-') || test('+')) {
            return tryParseListEntry(c, indent)
        }

        return parseMarkup().also { test('\n') }
    }

    fun getMarkup(symbol: Char, text: MarkupText): MarkupText {
        return when (symbol) {
            '*' -> ::Emphasis
            '/' -> ::Italic
            '+' -> ::Strikeout
            '_' -> ::Underline
            else -> ::MarkupText
        }(listOf(), text)
    }

    fun parseMarkupWithSymbol(symbol: Char, prefix: String = ""): MarkupText {
        var stack: MarkupStack = MarkupStack(markup_symbols)
        var root: MarkupText = MarkupText()

        val id: Int = stack.push(symbol)

        var res = parseMarkup(stack = stack).also { test('\n') }

        if (stack.top().second != id) {
            root.add(Text(prefix + symbol, skipSpace = true))
            root.add(res)
        } else {
            if (prefix != "") root.add(Text(prefix, skipSpace = true))
            root.add(res)
            res = parseMarkup()
            root.add(res)
        }

        return root
    }

    fun parseMarkup(root: MarkupText = MarkupText(), stack: MarkupStack = MarkupStack(markup_symbols), prefix: String = ""): MarkupText {

        var word: StringBuilder = StringBuilder(prefix)

        var beginOfLine: Boolean = true

        while (!src.isEof() && src.getChar() != '\n') {
            if (test('\\')) {
                raw_markup.append('\\')
                if (test('\\')) {
                    raw_markup.append('\\')
                    if (test('\n')) {
                        if (!word.isEmpty()) root.add(Text(word.toString()))
                        root.add(LineBreak())
                        stack.clear() // FIXME pop?
                        return root
                    } else {
                        word.append('\\')
                    }
                } else {
                    raw_markup.append(src.getChar())
                    word.append(src.getChar())
                    src.nextChar()
                }
                continue
            }

            if (test('[')) {
                if (test('[')) {
                    raw_markup = StringBuilder()
                    var id: Int = stack.push(']')
                    var content: MarkupText = parseMarkup(stack = stack)
                    if (stack.top().second != id) {
                        root.add(Text("[[", skipSpace = true))
                        root.add(content)
                    } else {
                        stack.pop()
                        test(']')
                        if (test(']')) {
                            root.add(Link(raw_markup.toString()))
                        } else if (test('[')) {
                            var url: String = raw_markup.toString()
                            id = stack.push(']')
                            var link_text: MarkupText = parseMarkup(stack = stack)
                            if (stack.top().second != id) {
                                root.add(Text("[[", skipSpace = true))
                                root.add(content)
                                root.add(Text("][", skipSpace = true))
                                root.add(link_text)
                            } else {
                                stack.pop()
                                test(']')
                                if (test(']')) {
                                    root.add(Link(url, other = link_text))
                                } else {
                                    root.add(Text("[[", skipSpace = true))
                                    root.add(content)
                                    root.add(Text("][", skipSpace = true))
                                    root.add(link_text)
                                    root.add(Text("]", skipSpace = src.getChar() != ' '))
                                }
                            }
                        } else {
                            root.add(Text("[[", skipSpace = true))
                            root.add(content)
                            root.add(Text("]", skipSpace = src.getChar() != ' '))
                        }
                    }
                } else {
                    word.append('[')
                    root.add(Text(word.toString(), skipSpace = src.getChar() != ' '))
                }
                continue
            }

            if (src.getChar() == ']') {
                if (stack.popUntil(']')) {
                    if (!word.isEmpty()) root.add(Text(word.toString(), skipSpace = true))
                    return root
                } else {
                    test(']')
                    raw_markup.append(']')
                    word.append(']')
                }
                continue
            }

            if (beginOfLine || test(' ')) {
                if (!beginOfLine) raw_markup.append(' ')
                if (!word.isEmpty()) root.add(Text(word.toString()))
                word = StringBuilder()

                var symbol: Char? = null

                for (s in markup_symbols) {
                    if (test(s)) {
                        raw_markup.append(s)
                        symbol = s
                        break
                    }
                }

                if (symbol != null) {
                    if (src.getChar() != ' ') {
                        val id: Int = stack.push(symbol)
                        if (symbol == '=') raw_markup = StringBuilder()
                        var content: MarkupText = parseMarkup(stack = stack)
                        if (stack.top().second != id) {
                            root.add(Text(symbol.toString(), skipSpace = true))
                        } else {
                            stack.pop()
                        }
                        root.add(content)
                    } else {
                        root.add(Text(symbol.toString()))
                    }
                }
                beginOfLine = false
            } else {
                // word.append(' ')
                var symbol: Char? = null

                for (s in markup_symbols) {
                    if (test(s)) {
                        symbol = s
                        break
                    }
                }

                if (symbol != null) {
                    if (src.getChar() == ' ' || src.getChar() == '\n' || src.isEof() || src.getChar() in markup_symbols) {
                        if (stack.popUntil(symbol)) {
                            if (symbol == '=') {
                                return Code(raw_markup.toString())
                            }
                            if (!word.isEmpty()) root.add(Text(word.toString(), skipSpace = src.getChar() in markup_symbols))
                            raw_markup.append(symbol)
                            return getMarkup(symbol, root)
                        } else {
                            word.append(symbol)
                        }
                        raw_markup.append(symbol)
                    } else {
                        raw_markup.append(symbol)
                        word.append(symbol)
                    }
                } else {
                    raw_markup.append(src.getChar())
                    word.append(src.getChar())
                    src.nextChar()
                }
            }
        }
        if (!word.isEmpty()) root.add(Text(word.toString()))
        stack.pop()

        return root
    }

    fun tryParseHeader(): Org {

        var level: Int = 1
        while (test('*')) level++

        return if (!test(' ')) {
            var prefix: String = ""
            for (i in 1..level - 1) prefix += '*'
            parseMarkupWithSymbol('*', prefix)
        } else Section(parseMarkup().also { test('\n') }, level)
    }

    fun tryParseListEntry(firstChar: Char, indent: Int): Org {
        var bullet: String = "" + firstChar

        if (firstChar in '0'..'9') {
            var c: Char = src.getChar()
            while (testRange('0'..'9')) {
                bullet += c
                c = src.getChar()
            }

            if (!test('.')) return parseMarkup(prefix = bullet).also { test('\n') }
            bullet += "."
        }

        if (!test(' ')) {
            if (firstChar in markup_symbols) {
                return parseMarkupWithSymbol(firstChar)
            }

            return parseMarkup(root = MarkupText(listOf(Text(bullet, skipSpace = true)))).also { test('\n') }
        }

        return ListEntry(parseMarkup().also { test('\n') }, bullet, indent)
    }

    data class ListResult(var entry: Org, var indent: Int, var list: OrgList, var emptyLines: Int)

    /* FIXME i am ugly */
    fun parseList(entryVal: ListEntry): ListResult {

        var entry: ListEntry = entryVal
        var list: OrgList = OrgList(emptyList())
        var lines: List<MarkupText> = emptyList()

        var skip: Boolean = false
        var nextEntry: Org? = null
        var nextIndent: Int = 0

        var emptyLines: Int = 0

        while (!src.isEof()) {

            if (!skip) {
                nextIndent = skipWhitespaces()
                var firstChar: Char = src.getChar()
                if (testRange('0'..'9') || test('-') || test('+')) {
                    nextEntry = tryParseListEntry(firstChar, nextIndent)
                } else {
                    if (nextIndent != 0) {
                        nextEntry = parseMarkup().also { test('\n') }
                    } else {
                        nextEntry = parseLine()
                    }
                }
            }
            skip = false

            nextEntry ?: throw ParserException("Skipped with null value")

            if (nextEntry is MarkupText && nextEntry.isEmpty()) nextIndent = entry.indent + 1

            if (nextIndent == entry.indent) {
                if (!lines.isEmpty()) entry.add(Paragraph(lines))
                lines = emptyList()
                list.add(entry)
                if (nextEntry is ListEntry) {
                    entry = nextEntry
                } else {
                    return ListResult(nextEntry, nextIndent, list, emptyLines)
                }
            } else if (nextIndent < entry.indent) {
                if (!lines.isEmpty()) entry.add(Paragraph(lines))
                list.add(entry)
                return ListResult(nextEntry, nextIndent, list, emptyLines)
            } else {
                if (nextEntry is ListEntry) {
                    if (!lines.isEmpty()) entry.add(Paragraph(lines))
                    lines = emptyList()
                    val (a, b, c, d) = parseList(nextEntry)
                    nextEntry = a
                    nextIndent = b
                    emptyLines = d
                    entry.add(c)
                    skip = true
                } else {
                    if (nextEntry is MarkupText && nextEntry.isEmpty()) {
                        emptyLines++
                        if (!lines.isEmpty()) entry.add(Paragraph(lines))
                        lines = emptyList()
                    } else if (nextEntry is MarkupText) {
                        emptyLines = 0
                        lines += nextEntry
                    } else {
                        throw ParserException("Unknown type when parsing list")
                    }
                    if (emptyLines >= 2) {
                        if (!lines.isEmpty()) entry.add(Paragraph(lines))
                        list.add(entry)
                        return ListResult(nextEntry, nextIndent, list, emptyLines)
                    }
                }
            }
        }
        // ilya gay ©Arseniy
        if (!lines.isEmpty()) {
            entry.add(Paragraph(lines))
        }
        list.add(entry)
        return ListResult(entry, entry.indent, list, emptyLines)
    }
}
