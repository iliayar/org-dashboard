package orgmode.parser

import orgmode.ParserException

interface Parser<T> {
    fun parse(): T
}

abstract class AbstractParser<T>(src: Source) : Parser<T> {

    val src: Source = src

    public fun test(c: Char): Boolean {
        if (c == src.getChar()) {
            src.nextChar()
            return true
        } else {
            return false
        }
    }

    public fun testRange(range: CharRange): Boolean {
        for (c in range) {
            if (test(c)) return true
        }
        return false
    }

    public fun skipWhitespaces(): Int {
        var i: Int = 0

        while (src.getChar().isWhitespace() && src.getChar() != '\n') {
            i++
            src.nextChar()
        }

        return i
    }

    public fun expect(c: Char) {
        if (!test(c)) throw ParserException("Expected " + c + ", but " + src.getChar() + " met")
    }

    public fun expect(s: String) {
        for (c in s) expect(c)
    }
}
