package com.ssafy.member.model.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.ssafy.member.model.MemberDto;

public interface MemberMapper {

	int idCheck(String userId) throws SQLException;
	void joinMember(MemberDto memberDto) throws SQLException;
	MemberDto loginMember(Map<String, String> map) throws SQLException;
	void deleteMember(String userId) throws SQLException;
	void updateMember(MemberDto memberDto) throws SQLException;
	MemberDto changePassword(Map<String, String> map) throws SQLException;
	List<MemberDto> listMember(Map<String, String> map) throws SQLException;
	int totalMemberCount(Map<String, String> map) throws SQLException;
	MemberDto getMember(String userId) throws SQLException;
}