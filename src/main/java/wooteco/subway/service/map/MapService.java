package wooteco.subway.service.map;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import wooteco.subway.domain.Line;
import wooteco.subway.domain.Station;
import wooteco.subway.domain.path.Graph;
import wooteco.subway.domain.path.Path;
import wooteco.subway.domain.path.WeightStrategy;
import wooteco.subway.domain.path.WeightType;
import wooteco.subway.dto.LineDetailResponse;
import wooteco.subway.dto.PathResponse;
import wooteco.subway.dto.StationResponse;
import wooteco.subway.dto.WholeSubwayResponse;
import wooteco.subway.repository.LineRepository;
import wooteco.subway.repository.StationRepository;

@Service
public class MapService {
    private LineRepository lineRepository;
    private StationRepository stationRepository;

    public MapService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    public WholeSubwayResponse wholeLines() {
        List<Line> lines = lineRepository.findAll();
        return lines.stream()
            .map(line -> {
                List<Station> stations = stationRepository.findAllById(line.getLineStationsId());
                return LineDetailResponse.of(line, stations);
            })
            .collect(Collectors.collectingAndThen(Collectors.toList(), WholeSubwayResponse::of));
    }

    @Transactional(readOnly = true)
    public PathResponse searchPath(String sourceName, String targetName, String type) {
        validate(sourceName, targetName);

        List<Line> lines = lineRepository.findAll();
        List<Station> stations = stationRepository.findAll();
        Station sourceStation = findStationByName(stations, sourceName);
        Station targetStation = findStationByName(stations, targetName);

        WeightStrategy strategy = WeightType.findStrategy(type);
        Graph graph = new Graph(lines, stations, strategy);
        Path path = graph.createPath(sourceStation, targetStation);

        return new PathResponse(StationResponse.listOf(path.getVertexList()), path.distance(),
            path.duration());
    }

    private void validate(String source, String target) {
        if (source.equals(target)) {
            throw new IllegalArgumentException("출발역과 도착역은 같을 수 없습니다");
        }
    }

    private Station findStationByName(List<Station> stations, String source) {
        return stations.stream()
            .filter(station -> source.equals(station.getName()))
            .findFirst()
            .orElseThrow(NoSuchElementException::new);
    }
}
