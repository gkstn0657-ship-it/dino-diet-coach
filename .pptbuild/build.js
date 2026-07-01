const pptxgen = require("pptxgenjs");
const p = new pptxgen();
p.layout = "LAYOUT_WIDE"; // 13.33 x 7.5
p.author = "SSAFY 관통 프로젝트 팀";
p.title = "냠냠코치(DinoDiet) 발표";

// ===== palette (no '#') =====
const DARK="13403A", MINT="2FA98C", MINTDK="1C7A66", CORAL="E0556A",
      INK="2B2733", MUTE="726C78", CARD="E9F5F0", CODEBG="102A26",
      CODETX="CFEFE4", WHITE="FFFFFF", LINE="D7E6E0";
const W=13.33, H=7.5, M=0.7;
const HEAD="Cambria", BODY="Calibri", MONO="Consolas";
const sh = () => ({ type:"outer", color:"000000", blur:8, offset:3, angle:90, opacity:0.12 });

function footer(s, n, label){
  s.addText("🦕 냠냠코치 · DinoDiet", { x:M, y:H-0.45, w:5, h:0.3, fontFace:BODY, fontSize:9, color:MUTE });
  s.addText(String(n), { x:W-1.1, y:H-0.45, w:0.5, h:0.3, fontFace:BODY, fontSize:9, color:MUTE, align:"right" });
}
function title(s, t, kicker){
  if(kicker) s.addText(kicker, { x:M, y:0.5, w:W-2*M, h:0.3, fontFace:BODY, fontSize:12, color:MINTDK, bold:true, charSpacing:2 });
  s.addText(t, { x:M, y:0.78, w:W-2*M, h:0.7, fontFace:HEAD, fontSize:30, color:INK, bold:true });
}
function card(s,x,y,w,h,fill){ s.addShape(p.shapes.ROUNDED_RECTANGLE,{x,y,w,h,fill:{color:fill||WHITE},line:{color:LINE,width:1},rectRadius:0.08,shadow:sh()}); }

let pageNo=0;
function page(bg){ const s=p.addSlide(); if(bg) s.background={color:bg}; pageNo++; return s; }

/* ---------- 1. TITLE ---------- */
{ const s=page(DARK);
  s.addText("🦕", {x:0, y:1.5, w:W, h:1.4, align:"center", fontSize:90});
  s.addText("냠냠코치", {x:0,y:2.95,w:W,h:0.9,align:"center",fontFace:HEAD,fontSize:60,bold:true,color:WHITE});
  s.addText("DinoDiet", {x:0,y:3.85,w:W,h:0.5,align:"center",fontFace:BODY,fontSize:22,color:MINT,charSpacing:4});
  s.addText("내 데이터를 아는 AI 식단 코치", {x:0,y:4.5,w:W,h:0.5,align:"center",fontFace:BODY,fontSize:18,italic:true,color:"CFE7DF"});
  s.addText("SSAFY 관통 프로젝트   ·   [팀명]   ·   [팀원 이름]   ·   2026.0X", {x:0,y:5.6,w:W,h:0.4,align:"center",fontFace:BODY,fontSize:13,color:"9FC4BA"});
}

/* ---------- 2. 목차 ---------- */
{ const s=page(WHITE); title(s,"목차","CONTENTS");
  const items=[
    "01  기획 배경 · 목표","02  추진 계획 (일정)","03  구현 범위 · 집중 포인트",
    "04  개발 결과 (핵심 기술)","05  개발 환경 · 시스템 구조도","06  화면 흐름도 · 시연",
    "07  적용 패턴 · 핵심 알고리즘","08  AI 사용 보고서 (코드·프롬프트)","09  기대 효과","10  개발 후기"
  ];
  let x=M, y=1.9;
  items.forEach((it,i)=>{
    const col=i<5?0:1; const row=i%5;
    const cx=M+col*((W-2*M)/2)+col*0.2, cy=1.9+row*0.95;
    card(s,cx,cy,(W-2*M)/2-0.1,0.78,i<5?CARD:WHITE);
    s.addText(it,{x:cx+0.3,y:cy,w:(W-2*M)/2-0.6,h:0.78,valign:"middle",fontFace:BODY,fontSize:15,bold:true,color:INK});
  });
  footer(s,2);
}

/* ---------- 3. 기획 배경·목표 ---------- */
{ const s=page(WHITE); title(s,"기획 배경 · 목표","PLANNING");
  card(s,M,1.85,(W-2*M)/2-0.15,4.7,WHITE);
  s.addText("문제 인식",{x:M+0.35,y:2.1,w:5,h:0.4,fontFace:HEAD,fontSize:18,bold:true,color:CORAL});
  s.addText([
    {text:"일반 AI 챗봇은 '내 식단·목표'를 모른다",options:{bullet:true,breakLine:true,paraSpaceAfter:8}},
    {text:"→ 누구에게나 똑같은 일반론만 답함",options:{bullet:true,indentLevel:1,breakLine:true,paraSpaceAfter:14,color:MUTE}},
    {text:"다이어트 관리가 앱마다 흩어져 있다",options:{bullet:true,breakLine:true,paraSpaceAfter:8}},
    {text:"→ 기록·분석·운동·동기부여가 따로 논다",options:{bullet:true,indentLevel:1,color:MUTE}},
  ],{x:M+0.35,y:2.55,w:(W-2*M)/2-0.85,h:3.7,fontFace:BODY,fontSize:14,color:INK});

  const rx=M+(W-2*M)/2+0.15;
  card(s,rx,1.85,(W-2*M)/2-0.15,4.7,CARD);
  s.addText("목표 — 기본 기능 구현 + AI 적용 집중",{x:rx+0.35,y:2.1,w:(W-2*M)/2-0.7,h:0.4,fontFace:HEAD,fontSize:17,bold:true,color:MINTDK});
  s.addText([
    {text:"기본 명세: 식단 기록 · 음식 검색",options:{bullet:true,breakLine:true,paraSpaceAfter:10}},
    {text:"＋ AI 코치 (Tool Calling 개인화 답변)",options:{bullet:true,breakLine:true,paraSpaceAfter:8,bold:true,color:MINTDK}},
    {text:"＋ AI 식단 분석 · AI 영양 추정",options:{bullet:true,breakLine:true,paraSpaceAfter:8,bold:true,color:MINTDK}},
    {text:"＋ 개인화 목표 칼로리(설문→TDEE)",options:{bullet:true,breakLine:true,paraSpaceAfter:8}},
    {text:"＋ 챌린지 승인·자동 인증 · 물 섭취",options:{bullet:true,breakLine:true,paraSpaceAfter:8}},
    {text:"＋ 커뮤니티·팔로우 · 운영 콘솔",options:{bullet:true}},
  ],{x:rx+0.35,y:2.55,w:(W-2*M)/2-0.85,h:3.7,fontFace:BODY,fontSize:14,color:INK});
  footer(s,3);
}

/* ---------- 4. 추진 계획 ---------- */
{ const s=page(WHITE); title(s,"추진 계획","TIMELINE");
  const rows=[
    [{text:"단계",options:{bold:true,color:WHITE,fill:{color:MINTDK}}},{text:"기간",options:{bold:true,color:WHITE,fill:{color:MINTDK}}},{text:"주요 내용",options:{bold:true,color:WHITE,fill:{color:MINTDK}}}],
    ["기획·설계","1주","요구사항 분석, ERD/화면 설계, 기술 선정"],
    ["기본 기능","2주","회원/인증, 식단 기록·음식 검색(식약처)"],
    ["AI 고도화","2주","Tool Calling 코치, 식단 분석, 영양 추정"],
    ["실서비스화","1주","챌린지 승인·자동인증, 개인화 칼로리, 운영 콘솔"],
    ["통합·QA·발표","1주","머지, 버그 수정, 시연 준비"],
  ];
  s.addTable(rows,{x:M,y:1.9,w:W-2*M,colW:[2.2,1.6,(W-2*M)-3.8],rowH:0.6,
    fontFace:BODY,fontSize:13,color:INK,valign:"middle",align:"left",
    border:{pt:1,color:LINE},fill:{color:WHITE}});
  s.addText("※ 개인별 상세 일정 / 역할 분담은 팀에서 작성  [팀 입력]",{x:M,y:6.2,w:W-2*M,h:0.4,fontFace:BODY,fontSize:12,italic:true,color:MUTE});
  footer(s,4);
}

/* ---------- 5. 구현 범위 · 집중 지점 ---------- */
{ const s=page(WHITE); title(s,"구현 범위 · 우리가 집중한 지점","SCOPE & FOCUS");
  s.addText("식단 기록·검색 같은 기본 기능은 상용 서비스에도 있습니다. 우리는 이를 직접 구현해 풀스택을 익히고, 한정된 기간의 노력을 \"생성형 AI를 데이터 기반으로 붙이는 것\"에 집중했습니다.",
    {x:M,y:1.65,w:W-2*M,h:0.7,fontFace:BODY,fontSize:14,color:INK});
  const cw=(W-2*M-0.6)/3, cy=2.6, ch=3.7;
  const cols=[
    ["🧱","기본 구현 (토대)",["회원 / 인증 (JWT)","식단 기록 (사진 / 검색)","음식 DB (식약처)","MySQL · MyBatis CRUD"],CARD,MINTDK],
    ["⭐","집중 영역 (핵심 노력)",["AI 코치 (Tool Calling)","AI 식단 분석 · 영양 추정","개인화 목표 칼로리","기록 기반 자동 인증 챌린지"],WHITE,CORAL],
    ["🌱","한계 · 향후",["알림 (설계만)","소셜 기능 확장","외부 연동 고도화"],CARD,MINTDK],
  ];
  cols.forEach((c,i)=>{ const x=M+i*(cw+0.3);
    card(s,x,cy,cw,ch,c[3]);
    s.addText(c[0],{x:x,y:cy+0.3,w:cw,h:0.7,align:"center",fontSize:32});
    s.addText(c[1],{x:x+0.2,y:cy+1.05,w:cw-0.4,h:0.5,align:"center",fontFace:HEAD,fontSize:16,bold:true,color:c[4]});
    s.addText(c[2].map((t)=>({text:t,options:{bullet:true,breakLine:true,paraSpaceAfter:9}})),
      {x:x+0.45,y:cy+1.75,w:cw-0.75,h:ch-2.0,fontFace:BODY,fontSize:13,color:INK});
  });
  footer(s,5);
}

/* ---------- 6. 개발 결과 (핵심 기술) ---------- */
{ const s=page(WHITE); title(s,"개발 결과 — 핵심 기술","RESULT");
  const cw=(W-2*M-0.3)/2, ch=2.15;
  const data=[
    ["🤖","데이터 기반 AI (3층)","의도 분류 → Tool Calling으로 식단·프로필 조회 → 근거 기반 답변. 코치별 멀티턴 기억 + 안전장치."],
    ["🎯","개인화 목표 칼로리","활동량 설문 → BMR·TDEE 계산을 서버가 저장. 홈·식단·AI가 같은 값을 공유해 일관성 확보."],
    ["⚙️","실서비스 운영 자동화","챌린지 신청→승인, 일일 마감 자동 인증 배치, 빈 챌린지 정리, 멱등 시딩 자가복구."],
    ["🔒","보안","JWT access/refresh 자동 재발급, BCrypt, 권한 서버 재검사, 시크릿 환경변수 외부화."],
  ];
  data.forEach((d,i)=>{ const col=i%2,row=Math.floor(i/2);
    const x=M+col*(cw+0.3), y=1.9+row*(ch+0.25);
    card(s,x,y,cw,ch,WHITE);
    s.addText(d[0],{x:x+0.3,y:y+0.28,w:1,h:0.8,fontSize:34});
    s.addText(d[1],{x:x+1.35,y:y+0.32,w:cw-1.6,h:0.5,fontFace:HEAD,fontSize:18,bold:true,color:MINTDK});
    s.addText(d[2],{x:x+1.35,y:y+0.92,w:cw-1.65,h:1.05,fontFace:BODY,fontSize:13,color:INK,valign:"top"});
  });
  footer(s,6);
}

/* ---------- 7. 개발 환경 & 시스템 구조도 ---------- */
{ const s=page(WHITE); title(s,"개발 환경 · 전체 시스템 구조도","ARCHITECTURE");
  s.addText([
    {text:"Front  ",options:{bold:true,color:MINTDK}},{text:"Vue 3 · Pinia · Vite      ",options:{color:INK}},
    {text:"Back  ",options:{bold:true,color:MINTDK}},{text:"Spring Boot 3 · MyBatis · JWT      ",options:{color:INK}},
    {text:"Data/AI  ",options:{bold:true,color:MINTDK}},{text:"MySQL · Spring AI(GMS) · 식약처 OpenAPI",options:{color:INK}},
  ],{x:M,y:1.6,w:W-2*M,h:0.4,fontFace:BODY,fontSize:13});

  const box=(x,y,w,h,t,sub,fill,tc)=>{ card(s,x,y,w,h,fill);
    s.addText(t,{x:x,y:y+0.18,w:w,h:0.45,align:"center",fontFace:HEAD,fontSize:15,bold:true,color:tc||INK});
    if(sub) s.addText(sub,{x:x,y:y+0.66,w:w,h:0.4,align:"center",fontFace:BODY,fontSize:11,color:tc?"DDEFE9":MUTE}); };
  const arrow=(x,y,w)=>s.addShape(p.shapes.LINE,{x,y,w,h:0,line:{color:MINT,width:2.5}});
  const aw=(x,y)=>s.addText("›",{x,y:y-0.22,w:0.3,h:0.4,align:"center",fontFace:BODY,fontSize:20,bold:true,color:MINT});

  const y0=2.5;
  box(M,y0,2.3,1.0,"사용자","브라우저",WHITE);
  arrow(M+2.3,y0+0.5,0.5); aw(M+2.35,y0+0.5);
  box(M+2.85,y0,2.3,1.0,"Vue 3 SPA","화면·입력 흐름",CARD);
  arrow(M+5.15,y0+0.5,0.5); aw(M+5.2,y0+0.5);
  box(M+5.7,y0,2.55,1.0,"Spring Boot API","인증·식단·AI 연동",MINT,WHITE);
  arrow(M+8.25,y0+0.5,0.5); aw(M+8.3,y0+0.5);
  box(M+8.8,y0,2.3,1.0,"MySQL","MyBatis",WHITE);

  // AI / 식약처 branch
  const y1=4.3;
  box(M+5.7,y1,2.55,1.0,"Spring AI ChatClient","의도분류·Tool·생성",CARD);
  box(M+8.8,y1,2.3,1.0,"GMS","OpenAI 호환",WHITE);
  s.addShape(p.shapes.LINE,{x:M+8.25,y:y1+0.5,w:0.55,h:0,line:{color:MINT,width:2.5}});
  aw(M+8.3,y1+0.5);
  // connect API -> AI (vertical)
  s.addShape(p.shapes.LINE,{x:M+6.97,y:y0+1.0,w:0,h:y1-(y0+1.0),line:{color:MINT,width:2.5,dashType:"dash"}});

  const y2=5.85;
  box(M+5.7,y2,2.55,0.95,"식약처 OpenAPI","→ 로컬 foods 캐시",WHITE);
  s.addShape(p.shapes.LINE,{x:M+5.0,y:y2+0.47,w:0.7,h:0,line:{color:MINTDK,width:2,dashType:"dash"}});
  s.addText("음식 검색/수집",{x:M+2.85,y:y2+0.28,w:2.1,h:0.4,align:"center",fontFace:BODY,fontSize:10,color:MUTE});
  footer(s,7);
}

/* ---------- 8. 화면 흐름도 & 시연 ---------- */
{ const s=page(WHITE); title(s,"화면 흐름도 · 시연","DEMO");
  const steps=["회원가입","프로필 설문","식단 기록\n(사진/검색)","AI 분석·코칭","챌린지"];
  const bw=2.05, gap=0.35, y=1.95, x0=M;
  steps.forEach((t,i)=>{ const x=x0+i*(bw+gap);
    card(s,x,y,bw,1.1,i===0?CARD:WHITE);
    s.addText(t,{x:x,y:y,w:bw,h:1.1,align:"center",valign:"middle",fontFace:BODY,fontSize:13,bold:true,color:INK});
    if(i<steps.length-1) s.addText("›",{x:x+bw,y:y+0.3,w:gap,h:0.5,align:"center",fontFace:BODY,fontSize:22,bold:true,color:MINT});
  });
  // demo placeholder
  s.addShape(p.shapes.ROUNDED_RECTANGLE,{x:M,y:3.4,w:W-2*M,h:3.0,fill:{color:CODEBG},line:{color:MINTDK,width:1.5},rectRadius:0.1});
  s.addText("▶  시연 영상 / 화면 캡처 삽입 위치",{x:M,y:4.4,w:W-2*M,h:0.6,align:"center",fontFace:BODY,fontSize:20,bold:true,color:CODETX});
  s.addText("로그인 → 사진으로 식단 기록 → AI 분석 → 코치에게 질문(내 데이터 기반 답변) → 챌린지 인증   [영상 삽입]",
    {x:M,y:5.05,w:W-2*M,h:0.5,align:"center",fontFace:BODY,fontSize:12,color:"9FC4BA"});
  footer(s,8);
}

/* ---------- 9. 적용 패턴 & 핵심 알고리즘 ---------- */
{ const s=page(WHITE); title(s,"적용 패턴 · 핵심 알고리즘","PATTERN & ALGORITHM");
  const cw=(W-2*M-0.3)/2;
  card(s,M,1.85,cw,4.7,WHITE);
  s.addText("① Tool Calling 패턴",{x:M+0.35,y:2.1,w:cw-0.7,h:0.45,fontFace:HEAD,fontSize:18,bold:true,color:MINTDK});
  s.addText([
    {text:"질문 입력",options:{bullet:true,breakLine:true,paraSpaceAfter:6}},
    {text:"→ 의도 분류(LLM): 어떤 데이터 필요?",options:{bullet:true,breakLine:true,paraSpaceAfter:6}},
    {text:"→ 해당 Tool 호출로 DB 조회(식단·프로필·챌린지)",options:{bullet:true,breakLine:true,paraSpaceAfter:6}},
    {text:"→ 조회 결과 + 코치 페르소나로 답변 생성",options:{bullet:true,breakLine:true,paraSpaceAfter:10}},
    {text:"왜 그냥 LLM이 아닌가? — 환각 방지 + 내 실제 수치 반영",options:{italic:true,color:CORAL}},
  ],{x:M+0.35,y:2.6,w:cw-0.7,h:2.0,fontFace:BODY,fontSize:13,color:INK});
  s.addText("멀티턴: 최근 N턴만 컨텍스트(Sliding Window)로 비용·맥락 균형",
    {x:M+0.35,y:5.7,w:cw-0.7,h:0.7,fontFace:BODY,fontSize:12,color:MUTE,valign:"top"});

  const rx=M+cw+0.3;
  card(s,rx,1.85,cw,4.7,CARD);
  s.addText("② 개인화 목표 칼로리 알고리즘",{x:rx+0.35,y:2.1,w:cw-0.7,h:0.45,fontFace:HEAD,fontSize:18,bold:true,color:MINTDK});
  const algo=["BMR (Mifflin-St Jeor)","× 활동계수 (설문 5문항 · 0~20점)","= TDEE (내림)","× 목표계수 (감량0.85 / 유지1.0 / 증량1.15)","→ 10kcal 단위 반올림","→ 성별 안전범위 클램프 (남1500~4500 / 여1200~4000)"];
  algo.forEach((t,i)=>{ const y=2.65+i*0.6;
    s.addShape(p.shapes.OVAL,{x:rx+0.35,y:y,w:0.42,h:0.42,fill:{color:i===algo.length-1?CORAL:MINT}});
    s.addText(String(i+1),{x:rx+0.35,y:y,w:0.42,h:0.42,align:"center",valign:"middle",fontFace:BODY,fontSize:13,bold:true,color:WHITE});
    s.addText(t,{x:rx+0.95,y:y,w:cw-1.3,h:0.42,valign:"middle",fontFace:BODY,fontSize:13,color:INK});
  });
  footer(s,9);
}

/* ---------- 10. AI 사용 보고서 ① 기능표 + 코드 ---------- */
{ const s=page(WHITE); title(s,"AI 사용 보고서 ① — 기능 · 코드","AI REPORT");
  const hd=(t)=>({text:t,options:{bold:true,color:WHITE,fill:{color:MINTDK}}});
  const rows=[
    [hd("기능"),hd("AI 사용 방식"),hd("평가 유형")],
    ["AI 코치","Tool Calling으로 식단·프로필 조회 후 답변","데이터 검색/수집"],
    ["의도 분류","질문을 intent로 분류해 필요 데이터 결정","생성형 논리"],
    ["영양 추정","DB에 없는 음식 칼로리·영양 생성","생성형 데이터"],
    ["가이드 챗봇","문서(guide.md) 근거로만 답(환각 방지)","생성형+검색"],
  ];
  s.addTable(rows,{x:M,y:1.85,w:W-2*M,colW:[2.4,(W-2*M)-5.2,2.8],rowH:0.5,
    fontFace:BODY,fontSize:12.5,color:INK,valign:"middle",border:{pt:1,color:LINE}});

  s.addShape(p.shapes.ROUNDED_RECTANGLE,{x:M,y:4.65,w:W-2*M,h:1.95,fill:{color:CODEBG},rectRadius:0.06});
  s.addText("주요 코드 — Spring AI Tool Calling (AiService.java / NutritionTool.java)",
    {x:M+0.3,y:4.75,w:W-2*M-0.6,h:0.35,fontFace:BODY,fontSize:11,bold:true,color:MINT});
  s.addText([
    {text:"// 1) ChatClient에 Tool 연결 → 모델이 필요 시 DB 조회를 호출",options:{breakLine:true,color:"8FC9BB"}},
    {text:'String text = chatClient.prompt().messages(messages)',options:{breakLine:true,color:CODETX}},
    {text:'        .tools(tools)   // 식단·영양·프로필·챌린지 조회',options:{breakLine:true,color:CODETX}},
    {text:'        .call().content();',options:{breakLine:true,color:CODETX}},
    {text:"// 2) mno를 LLM 입력이 아닌 생성자 주입 → 타인 데이터 차단(보안)",options:{breakLine:true,color:"8FC9BB"}},
    {text:'@Tool(description="오늘 영양 요약(총kcal/목표/남은·단백·탄수·지방·끼니)")',options:{breakLine:true,color:CODETX}},
    {text:'public String getTodayNutritionSummary() { ... dietMapper.findByMember(mno,...); }',options:{color:CODETX}},
  ],{x:M+0.3,y:5.12,w:W-2*M-0.6,h:1.4,fontFace:MONO,fontSize:10.5,valign:"top"});
  footer(s,10);
}

/* ---------- 11. AI 사용 보고서 ② 프롬프트 ---------- */
{ const s=page(WHITE); title(s,"AI 사용 보고서 ② — 프롬프트","AI REPORT");
  // ROUTER_PROMPT
  s.addShape(p.shapes.ROUNDED_RECTANGLE,{x:M,y:1.85,w:W-2*M,h:2.55,fill:{color:CODEBG},rectRadius:0.06});
  s.addText("프롬프트 1 — 의도 분류기 (ROUTER_PROMPT) · 생성형 논리 구성",
    {x:M+0.3,y:1.95,w:W-2*M-0.6,h:0.35,fontFace:BODY,fontSize:11,bold:true,color:MINT});
  s.addText([
    {text:"너는 DinoDiet AI 코치의 '의도 분석기'다. 메시지를 읽고 JSON 하나만 출력한다.",options:{breakLine:true,color:CODETX}},
    {text:'{ "primaryIntent", "secondaryIntents", "neededData", "emotionalTone", "needsAction" }',options:{breakLine:true,color:CODETX}},
    {text:"[intent] date_time · food_lookup · diet_summary · meal_advice · workout_advice ·",options:{breakLine:true,color:CODETX}},
    {text:"         challenge_check · app_guide · general_chat",options:{breakLine:true,color:CODETX}},
    {text:"[neededData] profile · today_nutrition · weekly_nutrition · active_challenges · food_nutrition",options:{breakLine:true,color:CODETX}},
    {text:"- 애매·감정 섞인 식사 고민 → meal_advice    - 챌린지/인증/조건 → challenge_check",options:{color:"8FC9BB"}},
  ],{x:M+0.3,y:2.32,w:W-2*M-0.6,h:2.0,fontFace:MONO,fontSize:10.5,valign:"top"});

  // estimateNutrition prompt
  s.addShape(p.shapes.ROUNDED_RECTANGLE,{x:M,y:4.6,w:W-2*M,h:2.0,fill:{color:CODEBG},rectRadius:0.06});
  s.addText("프롬프트 2 — AI 영양 추정 (estimateNutrition) · 생성형 데이터 구성",
    {x:M+0.3,y:4.7,w:W-2*M-0.6,h:0.35,fontFace:BODY,fontSize:11,bold:true,color:MINT});
  s.addText([
    {text:"[system] 너는 영양사다. 한국 음식에 익숙하다. 반드시 JSON만 출력한다.",options:{breakLine:true,color:CODETX}},
    {text:"[user] '{음식이름}' 1인분 기준 영양성분을 추정해 아래 JSON으로만 답해줘:",options:{breakLine:true,color:CODETX}},
    {text:'        {"kcal":정수,"protein":정수,"carbs":정수,"fat":정수}',options:{breakLine:true,color:CODETX}},
    {text:"→ 결과에 estimated:true 플래그 부여, 이름은 서버가 강제(모델 변경 방지)",options:{color:"8FC9BB"}},
  ],{x:M+0.3,y:5.07,w:W-2*M-0.6,h:1.45,fontFace:MONO,fontSize:10.5,valign:"top"});
  footer(s,11);
}

/* ---------- 12. 기대 효과 ---------- */
{ const s=page(WHITE); title(s,"기대 효과","IMPACT");
  const cw=(W-2*M-0.6)/3;
  const data=[
    ["🙆","사용자","흩어진 다이어트 관리를 한 곳에서. 내 몸에 맞는 코칭으로 지속률↑"],
    ["🧠","기술","생성형 AI를 환각 없이(문서근거)·데이터 기반(Tool)으로 붙이는 설계 검증"],
    ["🚀","확장","알림·소셜 등으로 확장 가능한 실서비스형 구조 확보"],
  ];
  data.forEach((d,i)=>{ const x=M+i*(cw+0.3);
    card(s,x,2.1,cw,3.3,i===1?CARD:WHITE);
    s.addText(d[0],{x:x,y:2.5,w:cw,h:0.9,align:"center",fontSize:46});
    s.addText(d[1],{x:x,y:3.5,w:cw,h:0.5,align:"center",fontFace:HEAD,fontSize:19,bold:true,color:MINTDK});
    s.addText(d[2],{x:x+0.35,y:4.1,w:cw-0.7,h:1.2,align:"center",fontFace:BODY,fontSize:13,color:INK});
  });
  footer(s,12);
}

/* ---------- 13. 개발 후기 ---------- */
{ const s=page(WHITE); title(s,"개발 후기","RETROSPECTIVE");
  s.addShape(p.shapes.ROUNDED_RECTANGLE,{x:M,y:1.9,w:4.3,h:4.5,fill:{color:CARD},line:{color:LINE,width:1},rectRadius:0.08});
  s.addText("📷  팀 사진 삽입",{x:M,y:3.9,w:4.3,h:0.6,align:"center",fontFace:BODY,fontSize:16,bold:true,color:MUTE});
  const rx=M+4.6, rw=W-M-rx;
  const notes=[["[팀원 A]","맡은 부분 / 배운 점 — [작성]"],["[팀원 B]","맡은 부분 / 배운 점 — [작성]"],["[팀원 C]","맡은 부분 / 배운 점 — [작성]"]];
  notes.forEach((n,i)=>{ const y=1.9+i*1.55;
    card(s,rx,y,rw,1.35,WHITE);
    s.addText(n[0],{x:rx+0.3,y:y+0.18,w:rw-0.6,h:0.4,fontFace:HEAD,fontSize:15,bold:true,color:MINTDK});
    s.addText(n[1],{x:rx+0.3,y:y+0.6,w:rw-0.6,h:0.6,fontFace:BODY,fontSize:13,color:INK});
  });
  s.addText("팁: 트러블슈팅 한 줄씩(식약처 외부 API 지연→로컬 DB 캐시 / 형상관리 정립)이 회고에 좋음",
    {x:M,y:6.55,w:W-2*M,h:0.35,fontFace:BODY,fontSize:11,italic:true,color:MUTE});
  footer(s,13);
}

/* ---------- 14. CLOSING ---------- */
{ const s=page(DARK);
  s.addText("🦕",{x:0,y:1.6,w:W,h:1.1,align:"center",fontSize:64});
  s.addText("감사합니다",{x:0,y:2.9,w:W,h:0.9,align:"center",fontFace:HEAD,fontSize:46,bold:true,color:WHITE});
  s.addText("\"내 식단·목표 데이터를 읽고 답하는 AI 코치\" — 냠냠코치",
    {x:0,y:3.95,w:W,h:0.5,align:"center",fontFace:BODY,fontSize:17,italic:true,color:"CFE7DF"});
  s.addText("Q & A",{x:0,y:4.9,w:W,h:0.6,align:"center",fontFace:BODY,fontSize:20,bold:true,color:MINT,charSpacing:4});
}

const out = "C:/SSAFY/관통프로젝트/docs/냠냠코치_발표_초안_claude_v2.pptx";
p.writeFile({ fileName: out }).then(()=>console.log("DONE:", out));
