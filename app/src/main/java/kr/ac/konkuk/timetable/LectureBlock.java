package kr.ac.konkuk.timetable;

import org.jsoup.select.Elements;

import java.io.Serializable;
import java.util.StringTokenizer;

public class LectureBlock implements Serializable {


    private String lecture_number, lecture_title, lecture_time,
            lecture_professor, lecture_div, lecture_credit,
            lecture_grade, lecture_haksu, lecture_foreign,
            lecture_type1, lecture_type2, lecture_remark;
    private int lecture_current, lecture_total, lecture_suguni;

    public LectureBlock() {
        this.lecture_current = 0;
        this.lecture_total = 0;
        this.lecture_suguni = 0;
    }

    // 웹사이트의 강의정보 한줄을 넣은 경우
    public LectureBlock(Elements elements) {
        this();
        setGrade(elements.get(0).text());
        this.lecture_haksu = elements.get(1).text()+" ";
        this.lecture_div = elements.get(2).text()+" ";
        this.lecture_number = elements.get(3).text()+" ";
        this.lecture_credit = elements.get(5).text() + "학점";
        setTitle(elements.get(4).select("u").text());
        this.lecture_time = elements.get(8).text()+" ";
        this.lecture_professor = elements.get(9).text()+" ";
        setForeign(elements.get(10).text());
        this.lecture_type1 = elements.get(12).text()+" ";
        this.lecture_type2 = elements.get(13).text()+" ";
        this.lecture_remark = elements.get(16).text()+" ";
    }

    // 파일에서 한 줄씩 불러와서 쓰는 생성자 (toString 형식과같음)
    public LectureBlock(String line) {
        this();
        StringTokenizer strToken = new StringTokenizer(line);
        this.lecture_grade = strToken.nextToken("/");
        this.lecture_haksu = strToken.nextToken("/");
        this.lecture_div = strToken.nextToken("/");
        this.lecture_number = strToken.nextToken("/");
        this.lecture_credit = strToken.nextToken("/");
        this.lecture_title = strToken.nextToken("/");
        this.lecture_time = strToken.nextToken("/");
        this.lecture_professor = strToken.nextToken("/");
        this.lecture_foreign = strToken.nextToken("/");
        this.lecture_type1 = strToken.nextToken("/");
        this.lecture_type2 = strToken.nextToken("/");
        this.lecture_remark = strToken.nextToken("/");
    }

    // 과목번호 설정
    public void setNumber(String number) {
        this.lecture_number = number;
    }

    // 제한학년 설정
    public void setGrade(String grade) {
        if(grade.equals("9")){
            this.lecture_grade = "전체학년";
        } else {
            this.lecture_grade = grade + "학년";
        }
    }

    // 원어강의 설정
    public void setForeign(String foreign) {
        if(foreign.equals("")){
            this.lecture_foreign = "해당없음";
        } else {
            this.lecture_foreign = foreign;
        }
    }

    // 강의명 설정
    public void setTitle(String title) {
        StringTokenizer titleToken = new StringTokenizer(title);
        this.lecture_title = titleToken.nextToken("(");
    }

    // 현재인원 설정
    public void setCurrent(String current) {
        try {
            this.lecture_current = Integer.parseInt(current);
        } catch (Exception e) {

        }
    }

    // 총 인원 설정
    public void setTotal(String total) {
        try {
            this.lecture_total = Integer.parseInt(total);
        } catch (Exception e) {

        }
    }

    // 수강바구니 인원 설정
    public void setSuguni(String suguni) {
        try {
            this.lecture_suguni = Integer.parseInt(suguni);
        } catch (Exception e) {

        }
    }

    // 각각의 요소들 얻어오는 함수 (get ~ 함수 모두)
    public String getNumber() {
        return lecture_number;
    }

    public String getTitle() {
        return lecture_title;
    }

    public String getTime() {
        return lecture_time;
    }

    public String getProfessor() {
        return lecture_professor;
    }

    public String getDiv() {
        return lecture_div;
    }

    public String getCredit() {
        return lecture_credit;
    }

    public String getGrade() {
        return lecture_grade;
    }

    public String getHaksu() {
        return lecture_haksu;
    }

    public String getType1() {
        return lecture_type1;
    }

    public String getType2() {
        return lecture_type2;
    }

    public String getForeign() {
        return lecture_foreign;
    }

    public String getRemark() {
        return lecture_remark;
    }

    public int getCurrent() {
        return lecture_current;
    }

    public int getTotal() {
        return lecture_total;
    }

    public int getSuguni() {
        return lecture_suguni;
    }

    // 인원 초과여부 함수
    public boolean isFull() {
        if (lecture_total > lecture_current)
            return false;
        else
            return true;
    }


    // 이 함수는 시간이 월-금, 1-18교시가 아니면 null처리하며, 그 외에는 시간에 맞는 배열을 반환함.
    public int[][] convertTime() {

        // 만약 시간이 비어있으면 null
        if (lecture_time.equals(""))
            return null;

        StringTokenizer token = new StringTokenizer(lecture_time);
        int[][] time = new int[18][5];

        // 한 주에 여러번 있을 수 있으므로 토큰이 끝날때까지 돌린다.
        while (token.hasMoreTokens()) {
            String key = token.nextToken(",").trim();
            if (key.length() < 5)
                return null;
            try {
                // 강의 시작시간과 끝시간을 숫자로 변경, 배열 0~17임으로 -1함, 그 외에는 예외처리됨
                int start = Integer.parseInt(key.substring(1, 3)) - 1;
                int stop = Integer.parseInt(key.substring(4, 6)) - 1;

                // 만약 1교시부터 18교시가 아닌 경우 (야간시간대는 미지원)
                if (!(0 <= start && start < time.length && 0 <= stop && stop < time.length))
                    return null;

                // 요일 확인 코드
                int day = 0;
                switch (key.substring(0, 1)) {
                    case "월":
                        day = 0;
                        break;
                    case "화":
                        day = 1;
                        break;
                    case "수":
                        day = 2;
                        break;
                    case "목":
                        day = 3;
                        break;
                    case "금":
                        day = 4;
                        break;
                    default:
                        // 토요일 ,일요일, 기타 등등은 추가하지 않음.
                        return null;
                }
                // 강의가 있는 시간을 1로 바꿈
                for (int i = start; i <= stop; i++) {
                    time[i][day] = 1;
                }
            } catch (Exception e) {
                // 형식이 다른 경우 (즉, 이러닝 등에서 예외처리)
                return null;
            }
        }
        return time;
    }


    // 시간이 겹치는지 확인하는 함수. true 겹침, false 안겹침
    public boolean checkTime(int[][] time1) {

        // 시간을 배열로 변환
        int[][] time2 = this.convertTime();

        // 하나라도 정상적인 시간이 아닌 경우에는 오류
        if (time1 == null || time2 == null)
            return true;

        // 겹치는 시간 확인
        for (int i = 0; i < time1.length; i++) {
            for (int j = 0; j < time1[i].length; j++) {
                // 하나라도 겹치면
                if (time1[i][j] == 1 && time2[i][j] == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    // 과목번호가 같으면 같은 블록이라고 취급함.
    @Override
    public boolean equals(Object obj) {

        // 과목번호가 같으면 같은 블록
        LectureBlock sb = (LectureBlock) obj;
        if (sb.getNumber().equals(this.getNumber())) {
            return true;
        } else {
            return false;
        }
    }


    // 파일에 저장하기 위해 String으로 변환하는 함수
    @Override
    public String toString() {
        return lecture_grade + "/"
                + lecture_haksu + "/"
                + lecture_div + "/"
                + lecture_number + "/"
                + lecture_credit + "/"
                + lecture_title + "/"
                + lecture_time + "/"
                + lecture_professor + "/"
                + lecture_foreign + "/"
                + lecture_type1 + "/"
                + lecture_type2 + "/"
                + lecture_remark + "\n";
    }
}
