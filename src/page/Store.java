package page;

import java.util.ArrayList;
import java.util.HashMap;
import page.Page;
import page.AddrNode;
import page.Node;
import iconst.IConst;
import iconst.NodeCellTyp;
import iconst.PageTyp;

// bookTab has 1024 ptrs. each to different pageTab
// pageTab has 1024 ptrs. each to different page
// INTVAL page has 1024 ints
// other pages for non-int data values
// stackTab has 2 stacks: operands (nodes) and operators
// operand stack has Page of Lists; each list has 1024 AddrNodes
// operator stack has 256 Pages of bytes

public class Store implements IConst {

	private PageTab bookTab[];
	private PageTab stackTab;
	
	public Store() {
		PageTab pgtab;
		stackTab = new PageTab();
		bookTab = new PageTab[INTPGLEN];
		for (int i=0; i < INTPGLEN; i++) {
			bookTab[i] = null;
		}
		pgtab = new PageTab(PageTyp.INTVAL);
		bookTab[0] = pgtab;
	}
	
	public PageTab getPageTab(int idx) {
		return bookTab[idx];
	}
	
	public void setPageTab(int idx, PageTab pgtab) {
		bookTab[idx] = pgtab;
	}
	
	public Page getPage(int addr) {
		PageTab pgtab;
		Page page;
		int pageidx, pgtabidx;
		
		addr = addr >>> 12;
		pageidx = addr & 0x3FF;
		pgtabidx = addr >>> 10;
		pgtab = getPageTab(pgtabidx);
		page = pgtab.getPage(pageidx);
		return page;
	}
	
	public Page getPageZero(int pgidx) {
		PageTab pgtab = bookTab[0];
		return pgtab.getPage(pgidx);
	}
	
	public int getElemIdx(int addr) {
		return (addr & 0xFFF);
	}
	
	public int getPageIdx(int addr) {
		return ((addr >>> 12) & 0x3FF);
	}
	
	public int getBookIdx(int addr) {
		return (addr >>> 22);
	}
	
	public boolean isLocAddr(int addr) {
		return (getBookIdx(addr) == 0);
	}
	
	public int allocInt(int val) {
		return allocVal(1, val, 0.0);
	}
	
	public int allocLong(long val) {
		return allocVal(2, val, 0.0);
	}
	
	public int allocDouble(double val) {
		return allocVal(3, 0, val);
	}
	
	private int allocVal(int typ, long ival, double dval) {
		PageTab pgtab;
		Page page;
		int idx;
		PageTyp pgtyp = PageTyp.INTVAL;
		
		switch (typ) {
		case 1:
			pgtyp = PageTyp.INTVAL;
			break;
		case 2:
			pgtyp = PageTyp.LONG;
			break;
		case 3:
			pgtyp = PageTyp.DOUBLE;
			break;
		}
		for (int i=0; i < INTPGLEN; i++) {
			pgtab = getPageTab(i);
			if (pgtab == null) {
				pgtab = new PageTab(pgtyp);
				setPageTab(i, pgtab);
			}
			for (int j=0; j < INTPGLEN; j++) {
				page = pgtab.getPage(j);
				if (page == null) {
					page = new Page(pgtyp);
					pgtab.setPage(j, page);
				}
				else if (page.getPageTyp() != pgtyp) {
					continue;
				}
				idx = -1; // no need
				switch (typ) {
				case 1:
					idx = page.allocInt((int) ival);
					break;
				case 2:
					idx = page.allocLong(ival);
					break;
				case 3:
					idx = page.allocDouble(dval);
					break;
				}
				if (idx >= 0) {
					return getAddr(i, j, idx);
				}
			}
		}
		return -1;
	}
	
	public int allocNode(Node node) {
		PageTab pgtab;
		Page page;
		int idx;
		
		for (int i=0; i < INTPGLEN; i++) {
			pgtab = getPageTab(i);
			if (pgtab == null) {
				pgtab = new PageTab(PageTyp.NODE);
				setPageTab(i, pgtab);
			}
			for (int j=0; j < INTPGLEN; j++) {
				page = pgtab.getPage(j);
				if (page == null) {
					page = new Page(PageTyp.NODE);
					pgtab.setPage(j, page);
				}
				else if (page.getPageTyp() != PageTyp.NODE) {
					continue;
				}
				idx = page.allocNode(node);
				if (idx >= 0) {
					return getAddr(i, j, idx);
				}
			}
		}
		return -1;
	}
	
	public int allocString(String str) {
		PageTab pgtab;
		Page page;
		int idx;
		
		for (int i=0; i < INTPGLEN; i++) {
			pgtab = getPageTab(i);
			if (pgtab == null) {
				pgtab = new PageTab(PageTyp.STRING);
				setPageTab(i, pgtab);
			}
			for (int j=0; j < INTPGLEN; j++) {
				page = pgtab.getPage(j);
				if (page == null) {
					page = new Page(PageTyp.STRING);
					pgtab.setPage(j, page);
				}
				else if (page.getPageTyp() != PageTyp.STRING) {
					continue;
				}
				idx = page.allocString(str);
				if (idx >= 0) {
					return getAddr(i, j, idx);
				}
			}
		}
		return -1;
	}
	
	public int allocList(ArrayList<AddrNode> list) {
		PageTab pgtab;
		Page page;
		int idx;
		
		for (int i=0; i < INTPGLEN; i++) {
			pgtab = getPageTab(i);
			if (pgtab == null) {
				pgtab = new PageTab(PageTyp.LIST);
				setPageTab(i, pgtab);
			}
			for (int j=0; j < INTPGLEN; j++) {
				page = pgtab.getPage(j);
				if (page == null) {
					page = new Page(PageTyp.LIST);
					pgtab.setPage(j, page);
				}
				else if (page.getPageTyp() != PageTyp.LIST) {
					continue;
				}
				idx = page.allocList(list);
				if (idx >= 0) {
					return getAddr(i, j, idx);
				}
			}
		}
		return -1;
	}
	
	public int allocMap(HashMap<String, AddrNode> map) {
		PageTab pgtab;
		Page page;
		int idx;
		
		for (int i=0; i < INTPGLEN; i++) {
			pgtab = getPageTab(i);
			if (pgtab == null) {
				pgtab = new PageTab(PageTyp.MAP);
				setPageTab(i, pgtab);
			}
			for (int j=0; j < INTPGLEN; j++) {
				page = pgtab.getPage(j);
				if (page == null) {
					page = new Page(PageTyp.MAP);
					pgtab.setPage(j, page);
				}
				else if (page.getPageTyp() != PageTyp.MAP) {
					continue;
				}
				idx = page.allocMap(map);
				if (idx >= 0) {
					return getAddr(i, j, idx);
				}
			}
		}
		return -1;
	}
	
	public int getAddr(int i, int j, int k) {
		int n;
		n = (i << 10) | j;
		n = (n << 12) | k;
		return n;
	}
	
	public long packHdrAddr(int header, int addr) {
		return stackTab.packHdrAddr(header, addr);
	}
	
	public boolean isNodeStkEmpty() {
		return stackTab.isNodeStkEmpty();
	}
	
	public boolean isOpStkEmpty() {
		return stackTab.isOpStkEmpty();
	}
	
	public AddrNode topNode() {
		return stackTab.topNode();
	}
	
	public AddrNode popNode() {
		return stackTab.popNode();
	}
	
	public boolean pushNode(AddrNode node) {
		return stackTab.pushNode(node);
	}

	public long topLong() {
		return stackTab.topLong();
	}
	
	public long popLong() {
		return stackTab.popLong();
	}
	
	public boolean pushLong(long val) {
		return stackTab.pushLong(val);
	}

	public byte topByte() {
		return stackTab.topByte();
	}
	
	public byte popByte() {
		return stackTab.popByte();
	}
	
	public boolean pushByte(byte byteval) {
		return stackTab.pushByte(byteval);
	}
	
	public Node getSubNode(Node node) {
		int downp;
		int idx;
		Page page;
		
		if (node.getDownCellTyp() != NodeCellTyp.PTR) {
			return null;
		}
		downp = node.getDownp();
		page = getPage(downp);
		idx = getElemIdx(downp);
		node = page.getNode(idx);
		return node;
	}

}

class PageTab implements IConst {
	
	private Page pageTab[];
	private Page nodepg;
	private int nodeStkLstIdx;
	private int nodeStkIdx;
	private int opStkPgIdx;
	private int opStkIdx;
	
	public PageTab(PageTyp pgtyp) {
		Page page;
		pageTab = new Page[INTPGLEN];
		for (int i=0; i < INTPGLEN; i++) {
			pageTab[i] = null;
		}
		page = new Page(pgtyp);
		pageTab[0] = page;
	}
	
	public PageTab() {
		Page page;
		ArrayList<AddrNode> list = initStkLst(NODESTKLEN);

		pageTab = new Page[OPSTKLEN];
		for (int i=0; i < OPSTKLEN; i++) {
			pageTab[i] = null;
		}
		opStkPgIdx = 0;
		opStkIdx = 0;
		page = new Page(PageTyp.BYTE);
		pageTab[opStkPgIdx] = page;

		nodeStkLstIdx = 0;
		nodeStkIdx = 0;
		nodepg = new Page(PageTyp.LIST);
		nodepg.setList(0, list);
		for (int i=1; i < INTPGLEN; i++) {
			nodepg.setList(i, null);
		}
	}
	
	public Page getPage(int idx) {
		return pageTab[idx];
	}
	
	public void setPage(int idx, Page page) {
		pageTab[idx] = page;
	}

	private ArrayList<AddrNode> initStkLst(int len) {
		ArrayList<AddrNode> list = new ArrayList<AddrNode>(); 
		AddrNode node;

		list.clear();
		for (int i=0; i < len; i++) {
			node = new AddrNode(0, 0);
			list.add(node);
		}
		return list;
	}
	
	public long packHdrAddr(int header, int addr) {
		long h;
		long rtnval;

		h = header & 0xFFFF;
		rtnval = (h << 32) | (addr & 0xFFFFFFFF);
		return rtnval;
	}
	
	public boolean isNodeStkEmpty() {
		return (nodeStkIdx == 0 && nodeStkLstIdx == 0);
	}
	
	public boolean isOpStkEmpty() {
		return (opStkIdx == 0 && opStkPgIdx == 0);
	}
	
	@SuppressWarnings("unchecked")
	public AddrNode topNode() {
		int idx;
		AddrNode node;
		ArrayList<AddrNode> list;
		
		if (nodeStkIdx > 0) {
			list = (ArrayList<AddrNode>) nodepg.getList(nodeStkLstIdx);
			node = list.get(nodeStkIdx - 1);
		}
		else if (nodeStkLstIdx > 0) {
			list = (ArrayList<AddrNode>) nodepg.getList(nodeStkLstIdx - 1);
			idx = NODESTKLEN - 1;
			node = list.get(idx);
		}
		else {
			node = null;
		}
		return node;
	}
	
	@SuppressWarnings("unchecked")
	public AddrNode popNode() {
		AddrNode node;
		ArrayList<AddrNode> list;
		
		if (nodeStkIdx > 0) {
			list = (ArrayList<AddrNode>) nodepg.getList(nodeStkLstIdx);
			node = list.get(--nodeStkIdx);
		}
		else if (nodeStkLstIdx > 0) {
			list = (ArrayList<AddrNode>) nodepg.getList(--nodeStkLstIdx);
			nodeStkIdx = NODESTKLEN - 1;
			node = list.get(nodeStkIdx);
		}
		else {
			node = null;
		}
		return node;
	}
	
	@SuppressWarnings("unchecked")
	public boolean pushNode(AddrNode node) {
		int header = node.getHeader();
		int addr = node.getAddr();
		AddrNode addrNode;
		ArrayList<AddrNode> list;

		if (nodeStkIdx < NODESTKLEN) { 
			list = (ArrayList<AddrNode>) nodepg.getList(nodeStkLstIdx);
		}
		else if (nodeStkLstIdx < INTPGLEN - 1) {
			list = (ArrayList<AddrNode>) nodepg.getList(++nodeStkLstIdx);
			if (list == null) {
				list = initStkLst(NODESTKLEN);
			}
			nodeStkIdx = 0;
		}
		else {
			return false;
		}
		addrNode = list.get(nodeStkIdx++);
		addrNode.setHeader(header);
		addrNode.setAddr(addr);
		return true;
	}

	@SuppressWarnings("unchecked")
	public long topLong() {
		int header, addr;
		int idx;
		long rtnval;
		AddrNode node;
		ArrayList<AddrNode> list;
		
		if (nodeStkIdx > 0) {
			list = (ArrayList<AddrNode>) nodepg.getList(nodeStkLstIdx);
			node = list.get(nodeStkIdx - 1);
		}
		else if (nodeStkLstIdx > 0) {
			list = (ArrayList<AddrNode>) nodepg.getList(nodeStkLstIdx - 1);
			idx = NODESTKLEN - 1;
			node = list.get(idx);
		}
		else {
			return -1;
		}
		header = node.getHeader();
		addr = node.getAddr();
		rtnval = packHdrAddr(header, addr);
		return rtnval;
	}
	
	@SuppressWarnings("unchecked")
	public long popLong() {
		int header, addr;
		long rtnval;
		AddrNode node;
		ArrayList<AddrNode> list;
		
		list = (ArrayList<AddrNode>) nodepg.getList(nodeStkLstIdx);
		if (nodeStkIdx > 0) {
			node = list.get(--nodeStkIdx);
		}
		else if (nodeStkLstIdx > 0) {
			list = (ArrayList<AddrNode>) nodepg.getList(--nodeStkLstIdx);
			nodeStkIdx = NODESTKLEN - 1;
			node = list.get(nodeStkIdx);
		}
		else {
			return -1;
		}
		header = node.getHeader();
		addr = node.getAddr();
		rtnval = packHdrAddr(header, addr);
		return rtnval;
	}
	
	@SuppressWarnings("unchecked")
	public boolean pushLong(long val) {
		int header = (int)(val >>> 32);
		int addr = (int) val;
		AddrNode addrNode;
		ArrayList<AddrNode> list;

		if (nodeStkIdx < NODESTKLEN) { 
			list = (ArrayList<AddrNode>) nodepg.getList(nodeStkLstIdx);
		}
		else if (nodeStkLstIdx < INTPGLEN - 1) {
			list = (ArrayList<AddrNode>) nodepg.getList(++nodeStkLstIdx);
			if (list == null) {
				list = initStkLst(NODESTKLEN);
			}
			nodeStkIdx = 0;
		}
		else {
			return false;
		}
		addrNode = list.get(nodeStkIdx++);
		addrNode.setHeader(header);
		addrNode.setAddr(addr);
		return true;
	}

	public byte topByte() {
		byte byteval;
		int idx;
		Page page = pageTab[opStkPgIdx];
		
		if (opStkIdx > 0) {
			byteval = page.getByte(opStkIdx - 1);
		}
		else if (opStkPgIdx > 0) {
			page = pageTab[opStkPgIdx - 1];
			idx = BYTPGLEN - 1;
			byteval = page.getByte(idx);
		}
		else {
			byteval = 0;
		}
		return byteval;
	}
	
	public byte popByte() {
		byte byteval;
		Page page = pageTab[opStkPgIdx];
		
		if (opStkIdx > 0) {
			byteval = page.getByte(--opStkIdx);
		}
		else if (opStkPgIdx > 0) {
			page = pageTab[--opStkPgIdx];
			opStkIdx = BYTPGLEN - 1;
			byteval = page.getByte(opStkIdx);
		}
		else {
			byteval = 0;
		}
		return byteval;
	}
	
	public boolean pushByte(byte byteval) {
		Page page;
		
		if (opStkIdx < BYTPGLEN) {
			page = pageTab[opStkPgIdx];
			page.setByte(opStkIdx++, byteval);
		}
		else if (opStkPgIdx < OPSTKLEN - 1) {
			page = pageTab[++opStkPgIdx];
			if (page == null) {
				page = new Page(PageTyp.BYTE);
			}
			opStkIdx = 0;
			page.setByte(opStkIdx++, byteval);
		}
		else {
			return false;
		}
		return true;
	}

}