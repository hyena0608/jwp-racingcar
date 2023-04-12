package racingcar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import racingcar.controller.dto.RacingInfoRequest;
import racingcar.controller.dto.RacingInfoResponse;
import racingcar.domain.CarGroup;
import racingcar.service.RacingGameService;

@RestController
public class RacingGameController {

    private final RacingGameService racingGameService;

    public RacingGameController(final RacingGameService racingGameService) {
        this.racingGameService = racingGameService;
    }

    @PostMapping("/plays")
    public ResponseEntity<RacingInfoResponse> playRacingGame(@RequestBody RacingInfoRequest request) {
        final String[] carNames = request.getNames().split(",");
        final CarGroup carGroup = new CarGroup(carNames);
        final RacingInfoResponse response = racingGameService.race(carGroup, request.getCount());

        return ResponseEntity.ok(response);
    }
}