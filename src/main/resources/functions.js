function getJobOrderIds(data) {
	if (typeof (data) == 'string') {
		data = JSON.parse(data);
	}
	if (!Array.isArray(data)) {
		return "";
	}
	return data.map(function(item) {
		return item.jobOrderID;
	}).join(',');
}