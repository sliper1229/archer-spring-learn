
package com.luo.spring.demo.action;
import com.luo.spring.demo.service.IModifyService;
import com.luo.spring.demo.service.IQueryService;
import com.luo.spring.framework.annotation.ArchAutowired;
import com.luo.spring.framework.annotation.ArchController;
import com.luo.spring.framework.annotation.ArchRequestMapping;
import com.luo.spring.framework.annotation.ArchRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 公布接口url
 * @author Tom
 *
 */
@ArchController
@ArchRequestMapping("/web")
public class MyAction {

	@ArchAutowired
	IQueryService queryService;
	@ArchAutowired
	IModifyService modifyService;

	@ArchRequestMapping("/query.json")
	public void query(HttpServletRequest request, HttpServletResponse response,
								@ArchRequestParam("name") String name){
		String result = queryService.query(name);
		out(response,result);
	}
	
	@ArchRequestMapping("/add*.json")
	public void add(HttpServletRequest request,HttpServletResponse response,
			   @ArchRequestParam("name") String name,@ArchRequestParam("addr") String addr){
		String result = modifyService.add(name,addr);
		out(response,result);
	}
	
	@ArchRequestMapping("/remove.json")
	public void remove(HttpServletRequest request,HttpServletResponse response,
		   @ArchRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		out(response,result);
	}
	
	@ArchRequestMapping("/edit.json")
	public void edit(HttpServletRequest request,HttpServletResponse response,
			@ArchRequestParam("id") Integer id,
			@ArchRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		out(response,result);
	}
	
	
	
	private void out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
