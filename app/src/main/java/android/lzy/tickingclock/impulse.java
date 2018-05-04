package android.lzy.tickingclock;

import org.litepal.crud.DataSupport;

public class impulse extends DataSupport{
    private String time;
    private String solved;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String isSolved() {
        return solved;
    }

    public void setSolved(String solved) {
        this.solved = solved;
    }
}
