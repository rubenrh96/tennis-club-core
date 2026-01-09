package com.in4everyall.tennisclubmanager.tennisclub.validator;

import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchRequest;
import com.in4everyall.tennisclubmanager.tennisclub.exception.SetException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WinnerValidatorImpl implements  WinnerValidator{

    public void validateWinner(MatchRequest req) {
        if (req.winnerLicense() == null || req.winnerLicense().isBlank()) {
            return;
        }

        int p1Sets = countSetsWon(req.set1P1(), req.set1P2())
                + countSetsWon(req.set2P1(), req.set2P2())
                + countSetsWon(req.set3P1(), req.set3P2());

        int p2Sets = countSetsWon(req.set1P2(), req.set1P1())
                + countSetsWon(req.set2P2(), req.set2P1())
                + countSetsWon(req.set3P2(), req.set3P1());

        if (p1Sets == p2Sets) {
            throw new SetException("No se puede determinar un ganador con los sets informados");
        }

        String expectedWinner = p1Sets > p2Sets ? req.player1License() : req.player2License();
        if (!expectedWinner.equals(req.winnerLicense())) {
            throw new SetException("El ganador indicado no coincide con el resultado de los sets");
        }
    }

    private int countSetsWon(Short a, Short b) {
        if (a == null || b == null) return 0;
        return a > b ? 1 : 0;
    }

}
