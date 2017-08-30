package highloadcup.service;

/**
 * Created by d.asadullin on 24.08.2017.
 */
public class AvgResponse {
    private int status;
    private Double avg;

    public AvgResponse(int status) {
        this.status = status;
    }

    public AvgResponse(int status, Double avg) {
        this.status = status;
        this.avg = avg;
    }

    public int getStatus() {
        return status;
    }

    public Double getAvg() {
        return avg;
    }

    @Override
    public String toString() {
        return "AvgResponse{" +
                "status=" + status +
                ", avg=" + avg +
                '}';
    }
}
