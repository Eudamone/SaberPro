package services;

import dto.InternResultReport;
import dto.ModulePerformance;
import dto.PeriodTrend;
import dto.ReportContextStats;
import dto.ScoreStatistics;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ReportExportService {

    public JSONObject buildPayload(InternResultReport report) {
        JSONObject root = new JSONObject();
        root.put("context", serializeContext(report.getContext()));
        root.put("internalGlobal", serializeScore(report.getInternalGlobal()));
        root.put("externalGlobal", serializeScore(report.getExternalGlobal()));
        root.put("trendVariation", report.getTrendVariation());
        root.put("periodTrends", serializeTrends(report.getPeriodTrends()));
        root.put("internalModules", serializeModules(report.getInternalModules()));
        root.put("externalModules", serializeModules(report.getExternalModules()));
        root.put("pairedModules", serializeModules(report.pairModulesWithExternalAverage()));
        root.put("bestModule", report.getBestModule().map(this::serializeModule).orElse(null));
        root.put("criticalModule", report.getCriticalModule().map(this::serializeModule).orElse(null));
        return root;
    }

    public String buildPrompt(InternResultReport report) {
        ReportContextStats context = report.getContext();
        String contextLine = summarizeContext(context);
        double internalAvg = round(report.getInternalGlobal().getAverage());
        double externalAvg = round(report.getExternalGlobal().getAverage());
        double variation = round(report.getTrendVariation());

        ModulePerformance best = report.getBestModule().orElse(null);
        ModulePerformance critical = report.getCriticalModule().orElse(null);

        String bestLine = best == null ? "N/A" : formatModuleLine(best);
        String criticalLine = critical == null ? "N/A" : formatModuleLine(critical);

        return (
                "REPORTE SABER PRO – INSTRUCCIONES PARA ANALIZAR EL JSON\n" +
                        "El campo 'report' contiene toda la información estadística.\n" +
                        "Tu tarea es generar un resumen ejecutivo claro, estructurado y analítico.\n\n" +

                        "Incluye estos elementos:\n" +
                        "1. Comparación global (interno vs externo).\n" +
                        "2. Comentarios sobre la tendencia histórica (variación %).\n" +
                        "3. Mejor competencia y por qué destaca.\n" +
                        "4. Competencia crítica y sus posibles implicaciones.\n" +
                        "5. Comparación de módulos emparejados cuando exista referencia.\n" +
                        "6. Alertas si hay brechas negativas o comportamientos atípicos.\n" +
                        "7. Consideraciones por tamaño de muestra.\n\n" +

                        "Datos base del contexto del reporte:\n" +
                        "Contexto: " + contextLine + "\n" +
                        "Población evaluada: " + context.getEvaluatedCount() + " estudiantes.\n" +
                        String.format(Locale.US,
                                "Promedio interno %.2f vs externo %.2f. Variación histórica %.2f%%.\n",
                                internalAvg, externalAvg, variation
                        ) +
                        "Mejor competencia: " + bestLine + "\n" +
                        "Competencia crítica: " + criticalLine + "\n\n" +

                        "Genera el análisis con redacción profesional, sin inventar datos, " +
                        "basándote únicamente en lo que aparece en el JSON."
        );
    }

    private JSONObject serializeContext(ReportContextStats context) {
        JSONObject json = new JSONObject();
        json.put("periods", toArray(context.getPeriods()));
        json.put("semesters", toArray(context.getSemesters()));
        json.put("areas", toArray(context.getAreas()));
        json.put("nbc", toArray(context.getNbc()));
        json.put("evaluatedCount", context.getEvaluatedCount());
        json.put("testedCount", context.getTestedCount());
        return json;
    }

    private JSONArray serializeTrends(List<PeriodTrend> trends) {
        JSONArray array = new JSONArray();
        for (PeriodTrend trend : trends) {
            JSONObject json = new JSONObject();
            json.put("period", trend.getPeriod());
            json.put("average", trend.getAverageScore());
            json.put("sampleSize", trend.getEvaluatedCount());
            array.put(json);
        }
        return array;
    }

    private JSONArray serializeModules(List<ModulePerformance> modules) {
        JSONArray array = new JSONArray();
        for (ModulePerformance module : modules) {
            array.put(serializeModule(module));
        }
        return array;
    }

    private JSONObject serializeModule(ModulePerformance module) {
        JSONObject json = new JSONObject();
        json.put("module", module.getModuleName());
        json.put("statistics", serializeScore(module.getStatistics()));
        if (module.getReferenceAverage() != null) {
            json.put("referenceAverage", module.getReferenceAverage());
        }
        if (module.getVariation() != null) {
            json.put("variation", module.getVariation());
        }
        return json;
    }

    private JSONObject serializeScore(ScoreStatistics stats) {
        JSONObject json = new JSONObject();
        json.put("average", stats.getAverage());
        json.put("stdDeviation", stats.getStandardDeviation());
        json.put("min", stats.getMin());
        json.put("max", stats.getMax());
        json.put("sampleSize", stats.getSampleSize());
        return json;
    }

    private JSONArray toArray(List<?> values) {
        return values == null ? new JSONArray() : new JSONArray(values);
    }

    private String summarizeContext(ReportContextStats context) {
        List<String> parts = new ArrayList<>();
        if (!context.getPeriods().isEmpty()) {
            parts.add("Periodos " + join(context.getPeriods()));
        }
        if (!context.getSemesters().isEmpty()) {
            parts.add("Semestres " + join(context.getSemesters()));
        }
        if (!context.getAreas().isEmpty()) {
            parts.add("Áreas " + join(context.getAreas()));
        }
        if (!context.getNbc().isEmpty()) {
            parts.add("NBC " + join(context.getNbc()));
        }
        return parts.isEmpty() ? "Sin filtros" : String.join(" | ", parts);
    }

    private String join(List<?> list) {
        return list.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    private String formatModuleLine(ModulePerformance module) {
        double avg = module.getStatistics().getAverage();
        Double reference = module.getReferenceAverage();
        String referenceText = reference == null ? "" : String.format(Locale.US, " (ref %.2f)", reference);
        return module.getModuleName() + String.format(Locale.US, " %.2f", avg) + referenceText;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
