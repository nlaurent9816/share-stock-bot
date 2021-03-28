package fr.nlaurent.sharestocks.beans

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DebtTest {

    val debt = Debt(Player(1, "Machin"), Player(2, "Truc"), 2)

    @Test
    fun testConstructor() {
        val debt = Debt(Player(3, "Nico"), Player(4, "Cubi"), -10)
        assert(debt.stocksCount == 10L)
        assert(debt.creditor == Player(3, "Nico"))
        assert(debt.debtor == Player(4, "Cubi"))
    }

    @Test
    fun testStockCount() {
        debt.stocksCount -= 10
        assert(debt.stocksCount == 8L)
        assert(debt.creditor == Player(1, "Machin"))
        assert(debt.debtor == Player(2, "Truc"))
    }

    @Test
    fun testAddDebt() {
        debt += debt
        assert(debt.stocksCount == 4L)
        assert(debt.creditor == Player(2, "Truc"))
        assert(debt.debtor == Player(1, "Machin"))

        debt += Debt(Player(2, "Truc"), Player(1, "Machin"), 5)
        assert(debt.stocksCount == 1L)
        assert(debt.creditor == Player(1, "Machin"))
        assert(debt.debtor == Player(2, "Truc"))
    }

    @Test
    fun testAddDebt_illegalArgument() {
        assertThrows<IllegalArgumentException>("Debt is not on the same players!") {
            debt += Debt(Player(3, "Nico"), Player(4, "Cubi"), 3)
        }
    }


}