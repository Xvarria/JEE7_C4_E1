package com.empresa.soa.servlet;

import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

/**
 * Servlet implementation class FibonacciServlet
 */
@WebServlet("/fib")
public class FibonacciServlet extends HttpServlet {
	private Map<Integer, Integer> cache;
	private static final long serialVersionUID = 1L;

	// Executed when servlet is first loaded into container.
	public void init() {
		cache = Collections.synchronizedMap(new HashMap<Integer, Integer>());
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FibonacciServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String num = request.getParameter("num");
		// Si no existe parámetro envie la lista completa
		if (num == null) {
			Collection<Integer> fibs = cache.values();
			sendTypedResponse(request, response, fibs);
		} else {
			try {
				Integer key = Integer.parseInt(num.trim());
				Integer fib = cache.get(key);
				if (fib == null)
					fib = -1;
				sendTypedResponse(request, response, fib);
			} catch (NumberFormatException e) {
				sendTypedResponse(request, response, -1);
			}
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String nums = request.getParameter("nums");
		if (nums == null)
			throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
		nums = nums.replace('[', '\0');
		nums = nums.replace(']', '\0');
		String[] parts = nums.split(", ");
		List<Integer> list = new ArrayList<Integer>();
		for (String next : parts) {
			int n = Integer.parseInt(next.trim());
			cache.put(n, getFibonacchi(n));
			list.add(cache.get(n));
		}
		sendTypedResponse(request, response, list + " added.");
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
	}

	/**
	 * Calcula el fibonacci del número enviado
	 * 
	 * @param n
	 *            número entero
	 * @return fibonacci n
	 */
	private int getFibonacchi(int n) {
		if (n < 0)
			throw new HTTPException(403);
		// Caso frontera
		if (n < 2)
			return n;
		// Retorne el cache
		if (cache.containsKey(n))
			return cache.get(n);
		if (cache.containsKey(n - 1) && cache.containsKey(n - 2)) {
			cache.put(n, cache.get(n - 1) + cache.get(n - 2));
			return cache.get(n);
		}

		// Calcule el valor
		int fib = 1, prev = 0;
		for (int i = 2; i <= n; i++) {
			int temp = fib;
			fib += prev;
			prev = temp;
		}
		cache.put(n, fib);
		return fib;

	}

	private void sendTypedResponse(HttpServletRequest request, HttpServletResponse response, Object data) {
		String responseType = request.getHeader("accept");

		// Si el cliente solicita text, html o XML
		if (responseType.contains("text/plain"))
			sendPlain(response, data);
		else if (responseType.contains("text/html"))
			sendHtml(response, data);
		else
			sendXml(response, data);
	}

	private void sendXml(HttpServletResponse response, Object data) {
		try {
			XMLEncoder enc = new XMLEncoder(response.getOutputStream());
			enc.writeObject(data.toString());
			enc.close();
		} catch (IOException e) {
			throw new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void sendHtml(HttpServletResponse response, Object data) {
		String html_start = "<html><head><title>sendHtml response</title></head><body><div>";
		String html_end = "</div></body></html>";
		String html_doc = html_start + data.toString() + html_end;
		sendPlain(response, html_doc);
	}

	private void sendPlain(HttpServletResponse response, Object data) {
		try {
			OutputStream out = response.getOutputStream();
			out.write(data.toString().getBytes());
			out.flush();
		} catch (IOException e) {
			throw new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

}
