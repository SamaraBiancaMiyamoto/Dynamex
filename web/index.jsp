<%-- 
    Document   : index
    Created on : 04 28, 26, 4:01:54 PM
    Author     : gabbipagkaliwangan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <title>AlgoPOS — Search &amp; Transaction Hub</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
    <link rel="icon" href="${pageContext.request.contextPath}/images/logo.png" />
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
        <a href="${pageContext.request.contextPath}/" class="active">Transaction Hub</a>
        <a href="${pageContext.request.contextPath}/inventory.jsp">Inventory Dashboard</a>
    </nav>
    <div class="clock" id="clock">--:--:--</div>
</header>

<main class="container">
    <div class="hub-grid">

        <section class="card">
            <div class="card-header">
                <div>
                    <h2>Search &amp; Transaction Hub</h2>
                    <div class="subtitle">Type a Product ID and press Enter to run Binary Search.</div>
                </div>
                <span class="units-saved-pill">O(log n) Search</span>
            </div>
            <div class="card-body">
                <div class="search-box">
                    <input type="number" id="searchInput" placeholder="Enter Product ID (e.g. 1042)" autofocus />
                    <button class="btn btn-primary" id="searchBtn">Add to Cart</button>
                </div>
                <div class="search-result" id="searchResult">
                    <span>Awaiting search&hellip;</span>
                    <span class="meta">Press Enter to search</span>
                </div>

                <div style="margin-top: 22px;">
                    <table class="cart-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Item</th>
                                <th style="text-align:right;">Price</th>
                                <th style="text-align:center;">Qty</th>
                                <th style="text-align:right;">Subtotal</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody id="cartBody">
                            <tr><td colspan="6">
                                <div class="cart-empty">
                                    <div class="big">&#128722;</div>
                                    <div>Your cart is empty.</div>
                                    <div style="font-size:12.5px;margin-top:4px;">Search a Product ID to begin.</div>
                                </div>
                            </td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>

        <aside class="card">
            <div class="card-header">
                <div>
                    <h2>Order Summary</h2>
                    <div class="subtitle">Live totals update with every item.</div>
                </div>
            </div>
            <div class="card-body">
                <div class="summary-row"><span>Items</span><span class="value" id="sumCount">0</span></div>
                <div class="summary-row"><span>Subtotal</span><span class="value" id="sumSubtotal">&#8369;0.00</span></div>
                <div class="summary-row"><span>VAT (12%)</span><span class="value" id="sumTax">&#8369;0.00</span></div>
                <div class="summary-row total"><span>Total Due</span><span class="value" id="sumTotal">&#8369;0.00</span></div>

                <button class="btn btn-primary btn-large" id="checkoutBtn" style="margin-top: 22px;" disabled>
                    Proceed to Checkout
                </button>
                <button class="btn btn-secondary" id="clearBtn" style="margin-top: 10px; width: 100%;">
                    Clear Cart
                </button>

                <div class="info-banner" style="margin-top: 22px;">
                    <span class="icon">i</span>
                    <span>
                        <strong>Algorithm:</strong> Binary Search runs in O(log n).
                        With 10,000 products, the worst case takes only ~14 comparisons.
                    </span>
                </div>
            </div>
        </aside>
    </div>
</main>

<div class="modal-backdrop" id="paymentModal">
    <div class="modal">
        <div class="modal-head">
            <div>
                <h2>Payment &amp; Optimization</h2>
                <div class="subtitle">Finalize transaction and optimize change distribution</div>
            </div>
            <button class="modal-close" id="closeModal">&times;</button>
        </div>
        <div class="modal-body">
            <!-- Left -->
            <div class="modal-left">
                <div class="due-row">
                    <span class="label-strong">Total Due</span>
                    <span class="amount" id="modalTotal">&#8369;0.00</span>
                </div>
                <div class="label-tiny">Cash Tendered</div>
                <input type="number" step="0.01" min="0" class="cash-input" id="cashInput" placeholder="0.00" />

                <div class="divider-row"><span>Subtotal</span><span id="modalSub">&#8369;0.00</span></div>
                <div class="divider-row"><span>Tax (12%)</span><span id="modalTax">&#8369;0.00</span></div>

                <div class="change-row">
                    <span class="label-strong">Change to Return</span>
                    <span class="change-amount" id="changeAmount">&#8369;0.00</span>
                </div>

                <div class="info-banner">
                    <span class="icon">i</span>
                    <span>
                        <strong>Optimization Active</strong><br/>
                        Dynamic Programming is calculating the minimum number of physical
                        units for the return.
                    </span>
                </div>
            </div>

            <div class="modal-right">
                <div class="breakdown-head">
                    <span class="label-tiny" style="color: var(--muted);">Optimal Breakdown</span>
                    <span class="units-saved-pill" id="savedPill">0 UNITS SAVED</span>
                </div>
                <div class="breakdown-list" id="breakdownList">
                    <div class="cart-empty" style="padding: 30px 10px;">
                        <div style="font-size:13px;">Enter cash tendered to compute change.</div>
                    </div>
                </div>
                <div class="totals-row">
                    <span>Total Units returned:</span>
                    <span class="total-units" id="totalUnits">0 units</span>
                </div>
            </div>
        </div>
        <div class="modal-foot">
            <button class="btn btn-ghost" id="backBtn">&larr; Back to Hub</button>
            <div class="actions">
                <button class="btn btn-secondary" id="printPreviewBtn">Print Preview</button>
                <button class="btn btn-primary" id="confirmBtn" disabled>Confirm &amp; Print Receipt</button>
            </div>
        </div>
    </div>
</div>

<div class="receipt-overlay" id="receiptOverlay">
    <div class="receipt" id="receipt"></div>
</div>

<div class="toast" id="toast"></div>

<script>
    window.CTX = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>