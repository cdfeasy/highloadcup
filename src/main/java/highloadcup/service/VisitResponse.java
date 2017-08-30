package highloadcup.service;

/**
 * Created by d.asadullin on 24.08.2017.
 */
public class VisitResponse {
    private int status;
    private String visits;

    public VisitResponse(int status) {
        this.status = status;
    }

    public VisitResponse(int status, String visits) {
        this.status = status;
        this.visits = visits;
    }

    public int getStatus() {
        return status;
    }

    public String getVisits() {
        return visits;
    }

    @Override
    public String toString() {
        return "VisitResponse{" +
                "status=" + status +
                ", visits=" + visits +
                '}';
    }
}
