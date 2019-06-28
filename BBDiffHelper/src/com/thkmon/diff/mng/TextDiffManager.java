package com.thkmon.diff.mng;

import com.thkmon.diff.data.StringList;
import com.thkmon.diff.util.LogUtil;
import com.thkmon.diff.util.StringUtil;

public class TextDiffManager {
	
	
	public void diffString(String str1, String str2) {
		try {
			StringList strList1 = StringUtil.makeStringList(str1);
			StringList strList2 = StringUtil.makeStringList(str2);
			
			diffStringList(strList1, strList2);
			
		} catch (Exception e) {
			LogUtil.debug(e);
		}
	}
	
	
	public void diffStringList(StringList strList1, StringList strList2) {
		
		int count1 = strList1.size();
		int count2 = strList2.size();
		int count = (count1 < count2) ? count1 : count2;
		
		// 내용 동일한 라인은 최대한 건너뛰고 시작한다.
		int i = 0;
		for (i=0; i<count; i++) {
			if (!strList1.get(i).equals(strList2.get(i))) {
				break;
			}
		}
		
		diffStringList(strList1, strList2, i);
	}
	
	
	public void diffStringList(StringList strList1, StringList strList2, int startRow) {
		
		StringList diffResultList = new StringList();
		
		// 테스트 코드
//		int[] arr2 = findSameLine(null, strList1, strList2, 16169, 16171);
//		if (arr2 == null) {
//			System.out.println("못찾음");
//		} else {
//			System.out.println(arr2[0] + " " + arr2[1]);
//			System.out.println(strList1.get(arr2[0]) + " " + strList2.get(arr2[1]));
//			
//		}
//		if(1==1) {
//			return;
//		}
		
		int lastIndex1 = strList1.size() - 1;
		int lastIndex2 = strList2.size() - 1;
		
		int originRow1 = startRow;
		int originRow2 = startRow;
		
		int preRow1 = startRow;
		int preRow2 = startRow;
		
		int axisRow1 = startRow;
		int axisRow2 = startRow;
		
		boolean bExitLoop = false;
		
		while (true) {
			int[] arr = findSameLine(null, strList1, strList2, axisRow1, axisRow2);
			if (arr == null) {
				preRow1 = originRow1 + 1;
				preRow2 = originRow2 + 1;
				
				axisRow1 = lastIndex1 + 1;
				axisRow2 = lastIndex2 + 1;
				
				bExitLoop = true;
				
			} else {
				preRow1 = axisRow1;
				preRow2 = axisRow2;
				
				axisRow1 = arr[0];
				axisRow2 = arr[1];
				
				originRow1 = arr[0];
				originRow2 = arr[1];
				
				if (axisRow1 == lastIndex1 && axisRow2 == lastIndex2) {
					bExitLoop = true;
				}
			}
			
			if (axisRow1 - preRow1 == 0 && axisRow2 - preRow2 == 0) {
				// 한 줄 차이. 스킵해도 됨
				
			} else if (axisRow1 - preRow1 > 0 && axisRow2 - preRow2 > 0) {
				printModifyInfo(diffResultList, strList1, strList2, preRow1, preRow2, axisRow1, axisRow2);
					
			} else if (axisRow1 - preRow1 > 0) {
				printDeleteInfo(diffResultList, strList1, preRow1, axisRow1);
				
			} else if (axisRow2 - preRow2 > 0) {
				printAddInfo(diffResultList, strList2, preRow2, axisRow2);
			}
			
			if (!bExitLoop) {
				if (axisRow1 + 1 > lastIndex1 && axisRow2 + 1 > lastIndex2) {
					bExitLoop = true;
					continue;
				}
			}
			
			if (axisRow1 < lastIndex1) {
				axisRow1++;
			}
				
			if (axisRow2 < lastIndex2) {
				axisRow2++;
			}
			
			if (bExitLoop) {
				break;
			}
		}
		
		if (diffResultList.size() > 0) {
			System.out.println("----------");
			System.out.println("경로 : ");
			System.out.println("");
			
			int diffResultCount = diffResultList.size();
			for (int i=0; i<diffResultCount; i++) {
				System.out.println(diffResultList.get(i));
			}
			
			// 마지막 부분 처리
			System.out.println("----------");
			
		} else {
			System.out.println("차이점 없음");
		}
	}
	
	public int[] findSameLine(StringList diffList, StringList strList1, StringList strList2, int rowToStart1, int rowToStart2) {
		int lastIndex1 = strList1.size() - 1;
		int lastIndex2 = strList2.size() - 1;
		
		if (rowToStart1 > lastIndex1 && rowToStart2 > lastIndex2) {
			return null;
			
		} else if (rowToStart1 > lastIndex1) {
			rowToStart1 = lastIndex1;
			
		} else if (rowToStart2 > lastIndex2) {
			rowToStart2 = lastIndex2;
		}
		
		int axisRow1 = rowToStart1;
		int axisRow2 = rowToStart2;
		
		int row1 = rowToStart1;
		int row2 = rowToStart2;
		
		String line1 = "";
		String line2 = "";
		
		boolean bLeftMode = true;
		int limitNum = 0;
		
		while (true) {
			if (bLeftMode) {
				line1 = strList1.get(axisRow1);
				line2 = strList2.get(row2);
				
			} else {
				line1 = strList1.get(row1);
				line2 = strList2.get(axisRow2);
			}
			
			// 내용 같으면 라인번호 리턴
			if (line1.trim().length() > 0 && line1.equals(line2)) {
				int[] arr = new int[2];
				if (bLeftMode) {
					arr[0] = axisRow1;
					arr[1] = row2;
				} else {
					arr[0] = row1;
					arr[1] = axisRow2;
				}
				return arr;
			}
			
			// 끝까지 왔으면 다음 로우를 검사하자.
			if (row1 == lastIndex1 && row2 == lastIndex2) {
				// 진짜 끝까지 왔으면 그만 검사하자.
				if (axisRow1 == lastIndex1 && axisRow2 == lastIndex2) {
					limitNum++;
					if (limitNum > 1) {
						limitNum = 0;
						break;
					}
					
				} else {
					limitNum++;
					if (limitNum > 1) {
						limitNum = 0;
						
						if (axisRow1 + 1 <= lastIndex1) {
							axisRow1++;
							row1 = axisRow1;
						}
						if (axisRow2 + 1 <= lastIndex2) {
							axisRow2++;
							row2 = axisRow2;
						}
					}
				}
			}
			
			// 레프트 모드이면 로우2를 증가시킴
			if (bLeftMode) {
				if (row2 + 1 <= lastIndex2) {
					row2++;
				}
				bLeftMode = !bLeftMode;
			} else {
				if (row1 + 1 <= lastIndex1) {
					row1++;
				}
				bLeftMode = !bLeftMode;
			}
		}
		
		return null;
	}
	
	
	private void printModifyInfo(StringList diffResultList, StringList strList1, StringList strList2, int preRow1, int preRow2, int axisRow1, int axisRow2) {
		StringBuffer asisBuff = new StringBuffer();
		for (int i=preRow1; i<axisRow1; i++) {
			if (i > preRow1) {
				asisBuff.append("\n");
			}
			asisBuff.append(strList1.get(i));
		}
		
		StringBuffer tobeBuff = new StringBuffer();
		for (int i=preRow2; i<axisRow2; i++) {
			if (i > preRow2) {
				tobeBuff.append("\n");
			}
			tobeBuff.append(strList2.get(i));
		}
		
		boolean bAsisEmpty = false;
		if (asisBuff.toString().replace("\r", "").replace("\n", "").replace("\t", "").trim().length() == 0) {
			bAsisEmpty = true;
		}
		
		boolean bTobeEmpty = false;
		if (tobeBuff.toString().replace("\r", "").replace("\n", "").replace("\t", "").trim().length() == 0) {
			bTobeEmpty = true;
		}
		
		if (!bAsisEmpty && !bTobeEmpty) {
			diffResultList.add("라인 : " + preRow1);
			diffResultList.add("내용 : 수정");
			diffResultList.add("[AS-IS]");
			diffResultList.add(asisBuff.toString());
			
			diffResultList.add("[TO-BE]");
			diffResultList.add(tobeBuff.toString());
			
		} else if (bAsisEmpty && !bTobeEmpty) {
			diffResultList.add("라인 : " + preRow1);
			diffResultList.add("내용 : 추가");
			diffResultList.add(tobeBuff.toString());
			
		} else if (!bAsisEmpty && bTobeEmpty) {
			diffResultList.add("라인 : " + preRow1);
			diffResultList.add("내용 : 삭제");
			diffResultList.add(asisBuff.toString());
		}
	}
	
	private void printDeleteInfo(StringList diffResultList, StringList strList1, int preRow1, int axisRow1) {
		StringBuffer buff = new StringBuffer();
		
		for (int i=preRow1; i<axisRow1; i++) {
			if (i > preRow1) {
				buff.append("\n");
			}
			buff.append(strList1.get(i));
		}
		
		boolean bEmpty = false;
		if (buff.toString().replace("\r", "").replace("\n", "").replace("\t", "").trim().length() == 0) {
			bEmpty = true;
		}
		
		if (!bEmpty) {
			diffResultList.add("라인 : " + preRow1);
			diffResultList.add("내용 : 삭제");
			diffResultList.add(buff.toString());
		}
	}
	
	private void printAddInfo(StringList diffResultList, StringList strList2, int preRow2, int axisRow2) {
		StringBuffer buff = new StringBuffer();
		
		for (int i=preRow2; i<axisRow2; i++) {
			if (i > preRow2) {
				buff.append("\n");
			}
			buff.append(strList2.get(i));
		}
		
		boolean bEmpty = false;
		if (buff.toString().replace("\r", "").replace("\n", "").replace("\t", "").trim().length() == 0) {
			bEmpty = true;
		}
		
		if (!bEmpty) {
			diffResultList.add("라인 : " + preRow2);
			diffResultList.add("내용 : 추가");
			diffResultList.add(buff.toString());
		}
	}
}