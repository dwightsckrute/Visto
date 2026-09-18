package com.dwightsckrute.visto

import com.dwightsckrute.visto.core.ui.components.BounceMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Los signos y los topes del rebote de las páginas.
 *
 * Es aritmética de dos funciones, y es exactamente donde falla este tipo de efecto: un signo al revés
 * y el contenido se separa cuando debería volver, o se queda separado para siempre. En el teléfono eso
 * se ve como "la página se ha quedado torcida", que es un síntoma sin pista.
 */
class BounceMathTest {

    private val max = 200f

    // ---------------- Deshacer ----------------

    @Test
    fun `sin separacion no hay nada que deshacer`() {
        assertEquals(0f, BounceMath.undo(offset = 0f, delta = 30f), 0.01f)
    }

    @Test
    fun `el dedo en el mismo sentido no deshace`() {
        // Separado hacia abajo y arrastrando hacia abajo: eso separa más, no devuelve.
        assertEquals(0f, BounceMath.undo(offset = 50f, delta = 20f), 0.01f)
        assertEquals(0f, BounceMath.undo(offset = -50f, delta = -20f), 0.01f)
    }

    @Test
    fun `el dedo en sentido contrario deshace lo que puede`() {
        assertEquals(-20f, BounceMath.undo(offset = 50f, delta = -20f), 0.01f)
        assertEquals(20f, BounceMath.undo(offset = -50f, delta = 20f), 0.01f)
    }

    /** Un gesto grande deshace lo que hay y no se pasa al otro lado. */
    @Test
    fun `deshacer nunca cruza el cero`() {
        val undo = BounceMath.undo(offset = 50f, delta = -300f)
        assertEquals(-50f, undo, 0.01f)
        assertEquals(0f, 50f + undo, 0.01f)
    }

    // ---------------- Separar ----------------

    @Test
    fun `al principio el contenido sigue al dedo`() {
        // Sin nada separado, la resistencia es cero: el primer píxel de gesto vale un píxel.
        assertEquals(10f, BounceMath.stretch(offset = 0f, delta = 10f, maxOffset = max), 0.01f)
    }

    @Test
    fun `la resistencia crece con lo ya separado`() {
        val alPrincipio = BounceMath.stretch(offset = 0f, delta = 10f, maxOffset = max)
        val aMitad = BounceMath.stretch(offset = max / 2, delta = 10f, maxOffset = max) - max / 2
        val casiAlTope = BounceMath.stretch(offset = max * 0.9f, delta = 10f, maxOffset = max) -
            max * 0.9f

        assertTrue("a mitad debería costar más que al principio", aMitad < alPrincipio)
        assertTrue("casi al tope debería costar más que a mitad", casiAlTope < aMitad)
        assertTrue("y seguir siendo un avance", casiAlTope > 0f)
    }

    @Test
    fun `nunca pasa del tope`() {
        assertEquals(max, BounceMath.stretch(offset = max, delta = 5_000f, maxOffset = max), 0.01f)
        assertEquals(-max, BounceMath.stretch(offset = -max, delta = -5_000f, maxOffset = max), 0.01f)
    }

    @Test
    fun `funciona igual hacia arriba`() {
        assertEquals(-10f, BounceMath.stretch(offset = 0f, delta = -10f, maxOffset = max), 0.01f)
        val aMitad = BounceMath.stretch(offset = -max / 2, delta = -10f, maxOffset = max) + max / 2
        assertTrue("debería resistir también hacia arriba", aMitad > -10f)
    }

    /** Sin tope no hay efecto: una pantalla de cero píxeles no separa nada. */
    @Test
    fun `sin tope no se separa`() {
        assertEquals(0f, BounceMath.stretch(offset = 10f, delta = 10f, maxOffset = 0f), 0.01f)
    }
}
