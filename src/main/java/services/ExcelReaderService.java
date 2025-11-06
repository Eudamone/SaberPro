package services;

import com.poiji.bind.Poiji;
import com.poiji.option.PoijiOptions;
import dto.EstudianteLoad;
import org.springframework.stereotype.Service;

import java.io.File;

import java.util.List;

@Service
public class ExcelReaderService {

    public List<EstudianteLoad> readEstudents(File file) {
        PoijiOptions options = PoijiOptions.PoijiOptionsBuilder
                                            .settings()
                                            .sheetIndex(0)
                                            .build();

        return Poiji.fromExcel(file, EstudianteLoad.class,options);
    }
}
