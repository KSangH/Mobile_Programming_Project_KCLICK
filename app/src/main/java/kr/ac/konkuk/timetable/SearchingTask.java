package kr.ac.konkuk.timetable;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class SearchingTask implements Runnable {

    // 필수 변수
    private int type; // 학과명 조회 = 0, 강의 조회 = 1, 학년별 인원조회 = 2
    private ErrorCodeBlock errorCode; // 에러 발생 여부
    private boolean errorBool = false; // 인원수 조회 시 에러여부
    private Handler handler; // 핸들러 선언

    // 타입별 변수
    private ArrayList<String> arrayList; // 타입 0인 경우에, 정보를 모두 가져온 다음 학과명 array를 저장할 adapter
    private HashMap<String, String> departmentCode; // 타입 0인 경우에, 학과명 Key로 매칭되는 숫자값을 Value로 저장

    private ArrayList<LectureBlock> lectureList; // 타입 1인 경우에, 강의정보를 모두 강의 리스트에 저장함.
    private String[] params; // 타입 1,2인 경우에, 파라미터배열, 길이에 따른 조회 조건이 다름

    private LectureBlock lectureBlock; // 타입 2인 경우에 강의 정보블럭
    private int[][] peopleBlock; // 타입 2인 경우에, 학년별 인원수 담을 변수


    // 학과 조회하는 생성자
    public SearchingTask(Handler handler, HashMap<String, String> departmentCode, ArrayList<String> arrayList, ErrorCodeBlock errorCode) { // 학과명 조회용 생성자
        this.type = 0;
        this.handler = handler;
        this.departmentCode = departmentCode;
        this.arrayList = arrayList;
        this.errorCode = errorCode;
    }

    // 강의 조회하는 생성자
    public SearchingTask(Handler handler, ArrayList<LectureBlock> lectureList, ErrorCodeBlock errorCode, String... params) { //
        this.type = 1;
        this.handler = handler;
        this.params = params;
        this.lectureList = lectureList;
        this.errorCode = errorCode;
    }

    // 학년별 인원 조회하는 생성자
    public SearchingTask(Handler handler, LectureBlock lectureBlock, int[][] peopleBlock, ErrorCodeBlock errorCode, String... params) { //
        this.type = 2;
        this.handler = handler;
        this.errorCode = errorCode;
        this.lectureBlock = lectureBlock;
        this.peopleBlock = peopleBlock;
        this.params = params;
    }

    @Override
    public void run() {
        try {
            if (type == 0) {
                // 학과명 정보 불러오기
                Document document = Jsoup.connect("https://kupis.konkuk.ac.kr/sugang/acd/cour/time/SeoulTimetableInfo.jsp").get();
                Elements elements = document.select("select[name=openSust] option");
                for (int i = 1; i < elements.size(); i++) {
                    // 학과명을 추가하는 어레이리스트와, 학과명과 매칭되는 코드를 저장하는 해쉬맵에 값을 저장한다.
                    arrayList.add(elements.get(i).text());
                    departmentCode.put(elements.get(i).text(), elements.get(i).attr("value"));
                }

            } else if (type == 1) {
                // 강의 조회하기.
                Document document;
                Elements elements = null;
                String keyWord = null;

                // 파라미터 수의 차이로 구분
                if (params.length == 4) {
                    // 과목명으로 조회하기
                    try {
                        params[3] = params[3].replaceAll(" ", "");
                        if(params[3].length() == 4){
                            Integer.parseInt(params[3]);
                            document = Jsoup.connect("https://kupis.konkuk.ac.kr/sugang/acd/cour/time/SeoulTimetableInfo.jsp").data("ltYy", params[0]).data("ltShtm", params[1]).data("sbjtId", params[3]).postDataCharset("EUC-KR").post();
                        } else {
                            throw new Exception();
                        }

                    }catch(Exception e){
                        document = Jsoup.connect("https://kupis.konkuk.ac.kr/sugang/acd/cour/time/SeoulTimetableInfo.jsp").data("ltYy", params[0]).data("ltShtm", params[1]).data("sbjtNm", params[3]).postDataCharset("EUC-KR").post();

                    }
                    elements = document.select("tr[class=table_bg_white]");
                } else if (params.length == 5) {
                    // 일반 (학과 + 이수구분)으로 조회하기
                    document = Jsoup.connect("https://kupis.konkuk.ac.kr/sugang/acd/cour/time/SeoulTimetableInfo.jsp").data("ltYy", params[0]).data("ltShtm", params[1]).data("openSust", params[3]).data("pobtDiv", params[4]).post();
                    elements = document.select("tr[class=table_bg_white]");
                } else if (params.length == 6) {
                    // 기교의 경우 세부영역으로 조회하기
                    document = Jsoup.connect("https://kupis.konkuk.ac.kr/sugang/acd/cour/time/SeoulTimetableInfo.jsp").data("ltYy", params[0]).data("ltShtm", params[1]).data("openSust", params[3]).data("pobtDiv", params[4]).data("cultCorsFld", params[5]).post();
                    elements = document.select("tr[class=table_bg_white]");
                } else if (params.length == 7) {
                    // 심교의 경우 3가지 영역 구분하여 조회하기
                    document = Jsoup.connect("https://kupis.konkuk.ac.kr/sugang/acd/cour/time/SeoulTimetableInfo.jsp").data("ltYy", params[0]).data("ltShtm", params[1]).data("openSust", params[3]).data("pobtDiv", params[4]).post();
                    elements = document.select("tr[class=table_bg_white]");
                    keyWord = params[5];
                }
                if (elements != null) {

                    LectureBlock[] blockArray = new LectureBlock[elements.size()];
                    Thread[] threadArray = new Thread[elements.size()];
                    for (int i = 1; i < elements.size(); i++) {
                        Elements elementFinal = elements.get(i).select("td");

                        // 만약 조회된 내용에서 td 가 여러개가 아닌 1개만 존재하면 조회된 내용이 없습니다를 의미한다.
                        if (elementFinal.size() == 1) {
                            break;
                        }
                        // 만약 심교의 경우 3가지 영역을 keyWord에 저장되어 있음.

                        // keyWord는 심교 외에는 null 이고, 심교인 경우에는 영역 체크 후에 추가한다.
                        if (keyWord == null || (keyWord != null && keyWord.equals(elementFinal.get(14).text()))) {
                            blockArray[i] = new LectureBlock(elementFinal);
                            threadArray[i] = new Thread(new CountTask(params[0], params[1], params[2], blockArray[i]));
                            threadArray[i].start();
                            lectureList.add(blockArray[i]);
                        }
                    }

                    // 스레드가 모두 종료될때까지 대기한다.
                    for (int i = 1; i < elements.size(); i++) {
                        if (threadArray[i] != null) {
                            threadArray[i].join();
                        }
                    }
                    // 스레드 내에서 에러 발생 여부
                    errorCode.setErrorCode(errorBool);
                }

            } else if (type == 2) {
                // 강의 정보 조회 시, 학년별 인원 수를 가져오는 함수
                Thread[] threadArray = new Thread[4];
                LectureBlock[] templb = new LectureBlock[4];

                // 1~4학년까지 정보를 받아온다.
                for (int i = 0; i < threadArray.length; i++) {
                    templb[i] = new LectureBlock();
                    templb[i].setNumber(lectureBlock.getNumber());
                    threadArray[i] = new Thread(new CountTask(params[0], params[1], (i + 1) + "", templb[i]));
                    threadArray[i].start();
                }
                // 모든 하위 스레드가 종료될 때까지 대기한다.
                for (int i = 0; i < threadArray.length; i++) {
                    if (threadArray[i] != null) {
                        threadArray[i].join();
                    }
                }

                // 하위스레드에서 에러 발생여부를 설정한다.
                errorCode.setErrorCode(errorBool);

                // 에러가 아닌 경우 각각의 정보를 인원수 블럭에 넣음
                if (!errorBool) {
                    for (int i = 0; i < templb.length; i++) {
                        peopleBlock[i][0] = templb[i].getSuguni();
                        peopleBlock[i][1] = templb[i].getCurrent();
                        peopleBlock[i][2] = templb[i].getTotal();
                    }
                }
            }
        } catch (Exception e) {
            errorCode.setErrorCode(true);
        }
        // 스레드가 종료됨을 핸들러를 통해 알린다
        if (handler != null) {
            Message msg = new Message();
            msg.what = type;
            handler.sendMessage(msg);
        }

    }

    // 인원 조회를 위한 클래스
    class CountTask implements Runnable {

        String year, semester, number, grade;
        LectureBlock block;

        public CountTask(String year, String semester, String grade, LectureBlock lectureBlock) {
            this.year = year;
            this.semester = semester;
            this.grade = grade;
            this.number = lectureBlock.getNumber();
            this.block = lectureBlock;
        }

        @Override
        public void run() {
            try {
                // grade = 0 은 전체학년 인원 조회이다.
                if (grade.equals("0")) {
                    Document document = Jsoup.connect("https://kupis.konkuk.ac.kr/sugang/acd/cour/aply/CourInwonInq.jsp").data("ltYy", year).data("ltShtm", semester).data("sbjtId", number).get();
                    Elements elements = document.select("div[class=box_nscroll2 mt25] td");
                    if (elements.size() < 2)
                        // 2개 이하이면 정보가 잘못된 것이다.
                        errorBool = true;
                    else {
                        // 현재인원, 전체인원 정보를 저장한다.
                        block.setCurrent(elements.get(0).text());
                        block.setTotal(elements.get(1).text());
                    }

                } else {
                    // 그 외 학년은 아래에서 인원 조회한다.
                    Document document = Jsoup.connect("https://kupis.konkuk.ac.kr/sugang/acd/cour/aply/CourBasketInwonInq.jsp").data("ltYy", year).data("ltShtm", semester).data("sbjtId", number).data("promShyr", grade).data("fg", "B").get();
                    Elements elements = document.select("div[class=box_nscroll2] td");
                    if (elements.size() < 2) {
                        // 2개 이하이면 정보가 잘못된 것이다.
                        errorBool = true;
                    } else {
                        // 수구니, 현재인원, 전체인원 정보를 저장한다.
                        block.setSuguni(elements.get(0).text());
                        StringTokenizer token = new StringTokenizer(elements.get(1).text());
                        block.setCurrent(token.nextToken("/").trim());
                        if(token.hasMoreTokens())
                            block.setTotal(token.nextToken("/").trim());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorBool = true;
            }
        }
    }
}


