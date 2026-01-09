package com.in4everyall.tennisclubmanager.tennisclub.validator;

import com.in4everyall.tennisclubmanager.tennisclub.exception.SetException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SetValidatorImpl implements SetValidator{

    public void validateSets(
            Short set1P1, Short set1P2,
            Short set2P1, Short set2P2,
            Short set3P1, Short set3P2
    ) {
        // 1) validar uno a uno si están informados
        validateSingleSet(set1P1, set1P2, 1);

        // si hay set2, debe haber set1
        if (set2P1 != null || set2P2 != null) {
            if (set1P1 == null || set1P2 == null) {
                throw new SetException("No se puede informar el set 2 si el set 1 está vacío");
            }
            validateSingleSet(set2P1, set2P2, 2);
        }

        // si hay set3, debe haber set2
        if (set3P1 != null || set3P2 != null) {
            if (set2P1 == null || set2P2 == null) {
                throw new SetException("No se puede informar el set 3 si el set 2 está vacío");
            }
            validateThirdSet(set3P1, set3P2);
        }
    }

    private void validateSingleSet(Short p1, Short p2, int setNumber) {
        if (p1 == null && p2 == null) {
            return; // set no jugado
        }
        if (p1 == null || p2 == null) {
            throw new SetException("El set " + setNumber + " debe tener los dos marcadores");
        }

        // rango básico
        if (p1 < 0 || p2 < 0 || p1 > 7 || p2 > 7) {
            throw new SetException("Marcador inválido en el set " + setNumber);
        }

        // 6-x normales
        if (p1 == 6 || p2 == 6) {
            int diff = Math.abs(p1 - p2);

            // 6-6 NO es válido aquí
            if (p1 == 6 && p2 == 6) {
                throw new IllegalArgumentException("Marcador inválido en el set " + setNumber);
            }

            // 6-0 .. 6-4 o 0-6 .. 4-6
            if (diff >= 2 && (p1 <= 6 && p2 <= 6)) {
                return; // válido
            }
            // si llega aquí, miramos los de 7
        }

        // 7-5 o 5-7
        if ((p1 == 7 && p2 == 5) || (p1 == 5 && p2 == 7)) {
            return;
        }

        // 7-6 o 6-7 (tiebreak)
        if ((p1 == 7 && p2 == 6) || (p1 == 6 && p2 == 7)) {
            return;
        }

        throw new SetException("Marcador inválido en el set " + setNumber);
    }

    private void validateThirdSet(Short p1, Short p2) {
        if (p1 == null && p2 == null) return;
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException("El set 3 debe tener los dos marcadores");
        }

        // 1) intentar como set normal (6/7). Si los dos están <=7, usamos la validación normal.
        boolean bothUnder8 = p1 <= 7 && p2 <= 7;
        if (bothUnder8) {
            validateSingleSet(p1, p2, 3);
            return;
        }

        // 2) Supertiebreak a 10
        int a = p1;
        int b = p2;

        if (a < 0 || b < 0) {
            throw new SetException("Marcador inválido en el set 3 (supertiebreak)");
        }

        int winner = Math.max(a, b);
        int loser  = Math.min(a, b);
        int diff   = Math.abs(a - b);

        // alguien debe llegar al menos a 10
        if (winner < 10) {
            throw new SetException("Marcador inválido en el set 3 (supertiebreak)");
        }

        // caso A: gana justo 10
        if (winner == 10) {
            // con 10 solo valen resultados 10-0 ... 10-8 (diferencia >=2 ya lo controla esto)
            if (diff < 2) {
                throw new SetException("Marcador inválido en el set 3 (supertiebreak)");
            }
            if (loser > 8) {
                // esto evitaría, por ejemplo, 10-9
                throw new SetException("Marcador inválido en el set 3 (supertiebreak)");
            }
            return;
        }

        // caso B: gana con más de 10 (11-9, 12-10, 13-11, ...)
        // aquí ya estamos en la fase de “ganar de 2 en 2”
        if (winner > 10) {
            // el que pierde tiene que haber llegado al menos a 9
            if (loser < 9) {
                // esto es lo que te molestaba: p.ej. 12-9 NO debe valer
                throw new SetException("Marcador inválido en el set 3 (supertiebreak)");
            }
            // y a partir de aquí la diferencia tiene que ser EXACTAMENTE 2 (11-9, 12-10, 13-11...)
            if (diff != 2) {
                throw new SetException("Marcador inválido en el set 3 (supertiebreak)");
            }
        }
    }

}
