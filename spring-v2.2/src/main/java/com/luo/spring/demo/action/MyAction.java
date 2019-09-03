
package com.luo.spring.demo.action;
import com.luo.spring.demo.service.IModifyService;
import com.luo.spring.demo.service.IQueryService;
import com.luo.spring.framework.annotation.ArchAutowired;
import com.luo.spring.framework.annotation.ArchController;
import com.luo.spring.framework.annotation.ArchRequestMapping;
import com.luo.spring.framework.annotation.ArchRequestParam;
import com.luo.spring.framework.webmvc.servlet.ArchModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@ArchController
@ArchRequestMapping("/web")
public class MyAction {

	@ArchAutowired IQueryService queryService;
	@ArchAutowired IModifyService modifyService;

	@ArchRequestMapping("/query.json")
	public ArchModelAndView query(HttpServletRequest request, HttpServletResponse response,
								  @ArchRequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}

	@ArchRequestMapping("/add*.json")
	public ArchModelAndView add(HttpServletRequest request,HttpServletResponse response,
							  @ArchRequestParam("name") String name,@ArchRequestParam("addr") String addr){
		String result = null;
		try {
			result = modifyService.add(name,addr);
			return out(response,result);
		} catch (Exception e) {
//			e.printStackTrace();
			Map<String,Object> model = new HashMap<String,Object>();
			model.put("detail",e.getMessage());
//			System.out.println(Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));
			model.put("stackTrace", Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));
			return new ArchModelAndView("500",model);
		}

	}

	@ArchRequestMapping("/remove.json")
	public ArchModelAndView remove(HttpServletRequest request,HttpServletResponse response,
								 @ArchRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}

	@ArchRequestMapping("/edit.json")
	public ArchModelAndView edit(HttpServletRequest request,HttpServletResponse response,
							   @ArchRequestParam("id") Integer id,
							   @ArchRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}



	private ArchModelAndView out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
