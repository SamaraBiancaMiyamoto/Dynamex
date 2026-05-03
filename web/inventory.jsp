<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <title>AlgoPOS — Inventory &amp; Resource Management</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
    <link rel="icon" href="${pageContext.request.contextPath}/images/logo.png" />
    <style>
        .algo-select {
            background: transparent;
            border: 1px solid var(--border);
            color: var(--navy);
            font-weight: 700;
            border-radius: 8px;
            padding: 8px;
            width: 100%;
            cursor: pointer;
            outline: none;
            font-family: inherit;
        }
        .algo-select:focus {
            border-color: var(--teal);
            box-shadow: 0 0 0 3px rgba(46, 191, 165, 0.1);
        }
    </style>
</head>
<body>

<header class="topbar">
    <div class="brand">
        <img src="${pageContext.request.contextPath}/images/logo.png" alt="AlgoPOS" />
        <div class="brand-text">
            <h1>AlgoPOS</h1>
            <small>Optimized Transaction System</small>
        </div>
    </div>
    <nav>
        <a href="${pageContext.request.contextPath}/">Transaction Hub</a>
        <a href="${pageContext.request.contextPath}/inventory.jsp" class="active">Inventory Dashboard</a>
    </nav>
    <div class="clock" id="clock">--:--:--</div>
</header>

<main class="container">

    <div class="metrics-grid" id="metricsGrid" style="grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));">
        <div class="metric-card">
            <div class="label">Dataset Size</div>
            <div class="value" id="mDatasetSize">&mdash;</div>
            <div class="delta">products loaded</div>
        </div>
        <div class="metric-card">
            <div class="label">Binary Search Time</div>
            <div class="value" id="mBinaryTime">&mdash;</div>
            <div class="delta" id="mBinaryComp">&mdash;</div>
        </div>
        <div class="metric-card">
            <div class="label">Linear Search Time</div>
            <div class="value" id="mLinearTime">&mdash;</div>
            <div class="delta" id="mSpeedup">&mdash;</div>
        </div>
        <div class="metric-card">
            <div class="label">DP Change Maker</div>
            <div class="value" id="mDpUnits">&mdash;</div>
            <div class="delta" id="mGreedyCmp">&mdash;</div>
        </div>
        <div class="metric-card">
            <div class="label">Calculation Engine</div>
            <div class="value" style="margin: 10px 0;">
                <select id="algoSelect" class="algo-select">
                    <option value="dp">Dynamic Programming</option>
                    <option value="greedy">Greedy Algorithm</option>
                </select>
            </div>
            <div class="delta" style="color: var(--teal);">Logic for checkout</div>
        </div>
    </div>

    <section class="card" style="margin-bottom: 24px;">
        <div class="card-header">
            <div>
                <h2>Inventory Table</h2>
                <div class="subtitle">Pre-sorted by Product ID — required for Binary Search.</div>
            </div>
            <button class="btn btn-secondary" id="refreshMetricsBtn">Run Algorithm Test</button>
        </div>
        <div class="card-body">
            <div class="inventory-toolbar">
                <input type="text" id="invSearch" placeholder="Filter by ID or name…" />
                <div class="pager">
                    <button id="firstBtn">&laquo;</button>
                    <button id="prevBtn">&lsaquo;</button>
                    <span class="page-info" id="pageInfo">Page 1 / 1</span>
                    <button id="nextBtn">&rsaquo;</button>
                    <button id="lastBtn">&raquo;</button>
                </div>
            </div>

            <table class="cart-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th style="text-align:right;">Price</th>
                        <th style="text-align:right;">Stock</th>
                    </tr>
                </thead>
                <tbody id="invBody"></tbody>
            </table>
        </div>
    </section>

    <section class="card">
        <div class="card-header">
            <div>
                <h2>Denomination Manager</h2>
                <div class="subtitle">
                    Toggle bills/coins as &ldquo;Out of Stock&rdquo; to test scarcity scenarios.
                </div>
            </div>
            <span class="units-saved-pill">Resource Management</span>
        </div>
        <div class="card-body">
            <div class="denom-toggles" id="denomList"></div>
        </div>
    </section>

</main>

<div class="toast" id="toast"></div>

<script>
    window.CTX = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/js/inventory.js"></script>
</body>
</html>