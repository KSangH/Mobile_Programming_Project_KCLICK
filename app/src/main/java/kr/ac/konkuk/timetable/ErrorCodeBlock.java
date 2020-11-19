package kr.ac.konkuk.timetable;


// 본 클래스는 에러 여부를 확인하기 위한 클래스입니다.
public class ErrorCodeBlock {
    private boolean errorCode;

    public ErrorCodeBlock(){
        this.errorCode = false; // 기본 값 false
    }

    public void setErrorCode(boolean errorCode) {
        this.errorCode = errorCode;
    }

    public boolean getErrorCode(){
        return this.errorCode;
    }
}