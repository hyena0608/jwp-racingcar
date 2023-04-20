package racingcar.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import racingcar.controller.response.RacingGameResponse;
import racingcar.domain.*;
import racingcar.dto.RacingGameDto;
import racingcar.repository.PlayerRepository;
import racingcar.repository.RacingGameRepository;

import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
public class RacingGameDatabaseService implements RacingGameService {
    private final RacingGameRepository racingGameRepository;
    private final PlayerRepository playerRepository;

    public RacingGameDatabaseService(
            final RacingGameRepository racingGameRepository,
            final PlayerRepository playerRepository
    ) {
        this.racingGameRepository = racingGameRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public RacingGameResponse race(final List<String> nameValues, final int trial) {
        final Cars cars = new Cars(nameValues);
        final RacingGame racingGame = new RacingGame(cars, new RandomNumberGenerator());
        racingGame.raceTimesBy(trial);
        final String winners = createWinners(racingGame);

        final int racingGameId = racingGameRepository.save(winners, trial);
        final int[] updatedCounts = playerRepository.saveAll(cars, racingGameId);
        validateAllSaved(nameValues, updatedCounts);

        return new RacingGameResponse(winners, cars.getRacingCars());
    }

    private void validateAllSaved(final List<String> nameValues, final int[] updatedCounts) {
        final boolean isAllSaved = updatedCounts.length == nameValues.size();
        if (!isAllSaved) {
            throw new IllegalStateException("[ERROR] 레이싱 플레이어 저장에 실패하였습니다.");
        }
    }

    private String createWinners(final RacingGame racingGame) {
        return racingGame.createRacingResult()
                .pickWinner()
                .stream()
                .map(Name::getValue)
                .collect(Collectors.joining(","));
    }

    @Override
    public List<RacingGameResponse> findAllRacingGameHistories() {
        return racingGameRepository.findAll()
                .stream()
                .map(this::parseToRacingGameResponseBy)
                .collect(Collectors.toList());
    }

    private RacingGameResponse parseToRacingGameResponseBy(final RacingGameDto racingGame) {
        return new RacingGameResponse(racingGame.getWinners(), parseToCarsBy(racingGame));
    }

    private List<Car> parseToCarsBy(final RacingGameDto racingGame) {
        return playerRepository.findByRacingGameId(racingGame.getId())
                .stream()
                .map(playerMapper -> new Car(playerMapper.getName(), playerMapper.getPosition()))
                .collect(Collectors.toList());
    }
}